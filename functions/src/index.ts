
import {setGlobalOptions} from "firebase-functions";
import {onCall, HttpsError} from "firebase-functions/v2/https";
import {initializeApp} from "firebase-admin/app";
import {
  getFirestore,
  FieldValue,
} from "firebase-admin/firestore";

initializeApp();

const db = getFirestore();

setGlobalOptions({maxInstances: 10});

const HOUR_IN_MS = 60 * 60 * 1000;

interface CreateBookingRequest {
  parkingId: string;
  startTime: number;
  endTime: number;
}

export const createBooking = onCall(
  async (request) => {
    // Require an authenticated user.
    if (!request.auth) {
      throw new HttpsError(
        "unauthenticated",
        "You must log in before booking."
      );
    }

    const driverId = request.auth.uid;
    const data = request.data as CreateBookingRequest;

    const parkingId = data?.parkingId;
    const startTime = data?.startTime;
    const endTime = data?.endTime;

    // Validate request.
    if (
      typeof parkingId !== "string" ||
      parkingId.trim() === "" ||
      !Number.isSafeInteger(startTime) ||
      !Number.isSafeInteger(endTime)
    ) {
      throw new HttpsError(
        "invalid-argument",
        "Invalid parking ID or booking schedule."
      );
    }

    const now = Date.now();

    if (startTime <= now || endTime <= startTime) {
      throw new HttpsError(
        "invalid-argument",
        "Please select a valid future booking schedule."
      );
    }

    const billableHours = Math.ceil(
      (endTime - startTime) / HOUR_IN_MS
    );

    const parkingRef = db
      .collection("parkings")
      .doc(parkingId);

    const driverRef = db
      .collection("users")
      .doc(driverId);

    // Every booking request for this parking facility
    // must use the same schedule lock.
    const lockRef = db
      .collection("parkingScheduleLocks")
      .doc(parkingId);

    const bookingRef = db
      .collection("bookings")
      .doc();

    try {
      const booking = await db.runTransaction(
        async (transaction) => {
          // All transaction reads happen before writes.
          const [parkingSnapshot, driverSnapshot, lockSnapshot] =
            await Promise.all([
              transaction.get(parkingRef),
              transaction.get(driverRef),
              transaction.get(lockRef),
            ]);

          if (!parkingSnapshot.exists) {
            throw new HttpsError(
              "not-found",
              "Parking facility not found."
            );
          }

          if (!driverSnapshot.exists) {
            throw new HttpsError(
              "failed-precondition",
              "Driver profile not found."
            );
          }

          const parking = parkingSnapshot.data()!;
          const driver = driverSnapshot.data()!;

          if (driver.role !== "DRIVER") {
            throw new HttpsError(
              "permission-denied",
              "Only drivers can create bookings."
            );
          }

          if (parking.active !== true) {
            throw new HttpsError(
              "failed-precondition",
              "This parking facility is not active."
            );
          }

          const ownerId = parking.ownerId;

          if (
            typeof ownerId !== "string" ||
            ownerId.length === 0
          ) {
            throw new HttpsError(
              "failed-precondition",
              "Parking owner information is missing."
            );
          }

          const totalSlots = parking.totalSlots;
          const pricePerHour = parking.pricePerHour;

          if (
            !Number.isInteger(totalSlots) ||
            totalSlots <= 0 ||
            typeof pricePerHour !== "number" ||
            !Number.isFinite(pricePerHour) ||
            pricePerHour <= 0
          ) {
            throw new HttpsError(
              "failed-precondition",
              "Invalid parking capacity or price."
            );
          }

          const ownerRef = db
            .collection("users")
            .doc(ownerId);

          const ownerSnapshot = await transaction.get(ownerRef);

          if (!ownerSnapshot.exists) {
            throw new HttpsError(
              "not-found",
              "Parking owner account not found."
            );
          }

          const owner = ownerSnapshot.data()!;

          if (
            owner.role !== "OWNER" ||
            typeof owner.phone !== "string" ||
            owner.phone.trim() === ""
          ) {
            throw new HttpsError(
              "failed-precondition",
              "Owner bKash phone number is unavailable."
            );
          }

          // Find bookings overlapping the requested period.
          const overlappingQuery = db
            .collection("bookings")
            .where("parkingId", "==", parkingId)
            .where("startTime", "<", endTime)
            .where("endTime", ">", startTime);

          const overlappingSnapshot =
            await transaction.get(overlappingQuery);

          let occupiedSlots = 0;

          overlappingSnapshot.forEach((document) => {
            const existing = document.data();

            const isActive =
              existing.bookingStatus === "PENDING" ||
              existing.bookingStatus === "CONFIRMED";

            if (isActive) {
              occupiedSlots +=
                Number(existing.reservedSlots) || 0;
            }
          });

          if (occupiedSlots + 1 > totalSlots) {
            throw new HttpsError(
              "resource-exhausted",
              "No parking slots are available for this time."
            );
          }

          // Calculate price on the trusted server.
          const totalAmount =
            billableHours * pricePerHour;

          const bookingData = {
            id: bookingRef.id,
            parkingId,
            parkingName: parking.name || "",
            ownerId,
            driverId,
            startTime,
            endTime,
            pricePerHour,
            durationHours: billableHours,
            totalAmount,
            ownerPhone: owner.phone,
            paymentMethod: "BKASH",
            transactionId: "",
            bookingStatus: "PENDING",
            paymentStatus: "PENDING",
            reservedSlots: 1,
            createdAt: now,
            updatedAt: now,
          };

          // Update the shared lock so simultaneous
          // booking transactions conflict and retry.
          transaction.set(
            lockRef,
            {
              version: (lockSnapshot.exists ?
                Number(lockSnapshot.data()?.version) || 0 : 0) + 1,
              updatedAt: FieldValue.serverTimestamp(),
            },
            {merge: true}
          );

          transaction.set(bookingRef, bookingData);

          return bookingData;
        }
      );

      return {
        success: true,
        message: "Booking request created successfully.",
        booking,
      };
    } catch (error) {
      if (error instanceof HttpsError) {
        throw error;
      }

      console.error("Booking creation failed:", error);

      throw new HttpsError(
        "internal",
        "Unable to create booking. Please try again."
      );
    }
  }
);

// ...existing code...
    public boolean canPatientBookSlot(int patientId, int doctorId, java.time.LocalDate date) {
        // Verify that the patient does not already have an appointment with this doctor on the same day
        return findAll().stream().noneMatch(a ->
                a.patientId() == patientId &&
                a.doctorId() == doctorId &&
                getSlotDateSafe(a) != null &&
                getSlotDateSafe(a).equals(date) &&
                !a.status().equals(com.clinicmanager.model.enums.AppointmentStatus.CANCELLED)
        );
    }
// ...existing code...// ...existing code...
    public boolean canPatientBookSlot(int patientId, int doctorId, java.time.LocalDate date) {
        // Verify that the patient does not already have an appointment with this doctor on the same day
        return findAll().stream().noneMatch(a ->
                a.patientId() == patientId &&
                a.doctorId() == doctorId &&
                getSlotDateSafe(a) != null &&
                getSlotDateSafe(a).equals(date) &&
                !a.status().equals(com.clinicmanager.model.enums.AppointmentStatus.CANCELLED)
        );
    }
// ...existing code...CREATE TABLE IF NOT EXISTS accounts (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    email TEXT NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    role TEXT NOT NULL,
    owner_id INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS doctors (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    date_of_birth TEXT,
    phone_number TEXT,
    schedule_id INTEGER
);

CREATE TABLE IF NOT EXISTS patients (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    date_of_birth TEXT,
    phone_number TEXT,
    medical_card_id INTEGER
);

CREATE TABLE IF NOT EXISTS medical_cards (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    patient_id INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS medical_records (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    medical_card_id INTEGER NOT NULL,
    doctor_id INTEGER NOT NULL,
    date TEXT NOT NULL,
    description TEXT
);

CREATE TABLE IF NOT EXISTS schedules (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    doctor_id INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS slots (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    schedule_id INTEGER NOT NULL,
    date TEXT NOT NULL,
    start_time TEXT NOT NULL,
    end_time TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS appointments (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    patient_id INTEGER NOT NULL,
    doctor_id INTEGER NOT NULL,
    slot_id INTEGER NOT NULL,
    status TEXT NOT NULL,
    problem_description TEXT
);

CREATE TABLE IF NOT EXISTS notifications (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    person_id INTEGER NOT NULL,
    message TEXT NOT NULL,
    timestamp TEXT NOT NULL,
    read BOOLEAN NOT NULL
);

CREATE TABLE IF NOT EXISTS favorite_doctors (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    patient_id INTEGER NOT NULL,
    doctor_id INTEGER NOT NULL,
    UNIQUE(patient_id, doctor_id)
);


CREATE TABLE IF NOT EXISTS accounts (
                                        id INTEGER PRIMARY KEY,
                                        email TEXT NOT NULL UNIQUE,
                                        password_hash TEXT NOT NULL,
                                        role TEXT NOT NULL,
                                        owner_id INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS doctors (
                                       id INTEGER PRIMARY KEY,
                                       name TEXT NOT NULL,
                                       date_of_birth TEXT,
                                       phone_number TEXT,
                                       schedule_id INTEGER
);

CREATE TABLE IF NOT EXISTS patients (
                                        id INTEGER PRIMARY KEY,
                                        name TEXT NOT NULL,
                                        date_of_birth TEXT,
                                        phone_number TEXT,
                                        medical_card_id INTEGER
);

CREATE TABLE IF NOT EXISTS medical_cards (
                                             id INTEGER PRIMARY KEY,
                                             patient_id INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS medical_records (
                                               id INTEGER PRIMARY KEY,
                                               medical_card_id INTEGER NOT NULL,
                                               doctor_id INTEGER NOT NULL,
                                               date TEXT NOT NULL,
                                               description TEXT
);

CREATE TABLE IF NOT EXISTS schedules (
                                         id INTEGER PRIMARY KEY,
                                         doctor_id INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS slots (
                                     id INTEGER PRIMARY KEY,
                                     schedule_id INTEGER NOT NULL,
                                     date TEXT NOT NULL,
                                     start_time TEXT NOT NULL,
                                     end_time TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS appointments (
                                            id INTEGER PRIMARY KEY,
                                            patient_id INTEGER NOT NULL,
                                            doctor_id INTEGER NOT NULL,
                                            slot_id INTEGER NOT NULL,
                                            status TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS notifications (
                                             id INTEGER PRIMARY KEY,
                                             person_id INTEGER NOT NULL,
                                             message TEXT NOT NULL,
                                             timestamp TEXT NOT NULL,
                                             read BOOLEAN NOT NULL
);
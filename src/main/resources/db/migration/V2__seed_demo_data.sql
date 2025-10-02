INSERT INTO doctors (id, name, date_of_birth, phone_number, schedule_id) VALUES
    (1, 'Dr. Emily Stone', '1980-03-12', '+1-555-1000', NULL),
    (2, 'Dr. Adam Novak', '1975-07-08', '+1-555-1001', NULL);

INSERT INTO schedules (id, doctor_id) VALUES
    (1, 1),
    (2, 2);

UPDATE doctors SET schedule_id = 1 WHERE id = 1;
UPDATE doctors SET schedule_id = 2 WHERE id = 2;

INSERT INTO patients (id, name, date_of_birth, phone_number, medical_card_id) VALUES
    (1, 'Alice Johnson', '1990-01-15', '+1-555-2000', NULL),
    (2, 'Bob Lee', '1988-09-20', '+1-555-2001', NULL);

INSERT INTO medical_cards (id, patient_id) VALUES
    (1, 1),
    (2, 2);

UPDATE patients SET medical_card_id = 1 WHERE id = 1;
UPDATE patients SET medical_card_id = 2 WHERE id = 2;

INSERT INTO slots (id, schedule_id, date, start_time, end_time) VALUES
    (1, 1, '2024-05-01', '09:00', '10:00'),
    (2, 1, '2024-05-01', '10:00', '11:00'),
    (3, 2, '2024-05-01', '09:00', '10:00'),
    (4, 2, '2024-05-02', '13:00', '14:00');

INSERT INTO appointments (id, patient_id, doctor_id, slot_id, status, problem_description) VALUES
    (1, 1, 1, 1, 'CONFIRMED', 'Routine check-up'),
    (2, 2, 2, 3, 'ENDED', 'Follow-up visit');

INSERT INTO medical_records (id, medical_card_id, doctor_id, date, description, appointment_id) VALUES
    (1, 1, 1, '2024-05-01', 'General health check. No issues found.', 1),
    (2, 2, 2, '2024-05-01', 'Post-treatment follow-up. Patient recovering well.', 2);

INSERT INTO notifications (id, person_id, message, timestamp, read) VALUES
    (1, 1, 'Your appointment with Dr. Stone is scheduled for 09:00.', '2024-04-28T08:15:00', 0),
    (2, 2, 'Your appointment summary is now available.', '2024-04-28T09:30:00', 0);

INSERT INTO favorite_doctors (id, patient_id, doctor_id) VALUES
    (1, 1, 1),
    (2, 2, 2);

INSERT INTO accounts (id, email, password_hash, role, owner_id) VALUES
    (1, 'doctor.stone@example.com', 'f348d5628621f3d8f59c8cabda0f8eb0aa7e0514a90be7571020b1336f26c113', 'DOCTOR', 1),
    (2, 'doctor.novak@example.com', 'f348d5628621f3d8f59c8cabda0f8eb0aa7e0514a90be7571020b1336f26c113', 'DOCTOR', 2),
    (3, 'alice.johnson@example.com', 'd4587ea9ead060c13fd994f21ecfa7926272a78854a2c20136b10a3c9e53e71e', 'PATIENT', 1),
    (4, 'bob.lee@example.com', 'd4587ea9ead060c13fd994f21ecfa7926272a78854a2c20136b10a3c9e53e71e', 'PATIENT', 2);

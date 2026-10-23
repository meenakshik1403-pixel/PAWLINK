package com.pawlink.medical;

import com.pawlink.pet.PetDAO;
import com.pawlink.pet.PetFactory;
import com.pawlink.user.UserDAO;
import com.pawlink.user.UserFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class RecordServiceTest {

    @Autowired
    private RecordService recordService;

    @Autowired
    private MedicalRecordDAO medicalRecordDAO;

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private PetDAO petDAO;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long vetId;
    private Long ownerId;
    private Long petId;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM reminders");
        jdbcTemplate.update("DELETE FROM vaccinations");
        jdbcTemplate.update("DELETE FROM medical_records");
        jdbcTemplate.update("DELETE FROM pets");
        jdbcTemplate.update("DELETE FROM users");

        // Seed Vet
        var vet = UserFactory.createUser("VET", null, "Dr. Sreekumar", "vet.sreekumar@pawlink.com", "555-0101", "hash", LocalDateTime.now());
        userDAO.insert(vet);
        vetId = vet.getUserId();

        // Seed Owner
        var owner = UserFactory.createUser("OWNER", null, "John Vargese", "john@pawlink.com", "555-0202", "hash", LocalDateTime.now());
        userDAO.insert(owner);
        ownerId = owner.getUserId();

        // Seed Pet
        var pet = PetFactory.createPet("Dog", "Bruno", LocalDate.of(2021, 5, 10), ownerId);
        petDAO.insert(pet);
        petId = pet.getPetId();
    }

    @Test
    @DisplayName("Should add medical record and populate treating vet name via SQL JOIN")
    void testAddMedicalRecordWithVetJoin() {
        MedicalRecord record = new MedicalRecord();
        record.setPetId(petId);
        record.setDiagnosis("Mild allergic dermatitis");
        record.setPrescription("Apoquel 16mg daily for 14 days");
        record.setRecordDate(LocalDate.now());

        MedicalRecord saved = recordService.addMedicalRecord(record, vetId);

        assertNotNull(saved.getId());
        assertEquals("Mild allergic dermatitis", saved.getDiagnosis());
        assertEquals("Dr. Sreekumar", saved.getVetName(), "Vet name must be populated via multi-table JOIN");

        List<MedicalRecord> list = medicalRecordDAO.findByPetId(petId);
        assertEquals(1, list.size());
        assertEquals("Dr. Sreekumar", list.get(0).getVetName());
    }

    @Test
    @DisplayName("Should add vaccination and automatically insert a reminder row in reminders table")
    void testAddVaccinationInsertsReminder() {
        LocalDate given = LocalDate.now();
        LocalDate nextDue = LocalDate.now().plusYears(1);

        VaccinationRecord vac = new VaccinationRecord();
        vac.setPetId(petId);
        vac.setVaccineName("Rabies 3-Year");
        vac.setGivenDate(given);
        vac.setNextDueDate(nextDue);

        VaccinationRecord saved = recordService.addVaccination(vac);

        assertNotNull(saved.getId());
        assertEquals("Rabies 3-Year", saved.getVaccineName());

        // Verify reminder row in P5 reminders table
        List<Map<String, Object>> reminders = jdbcTemplate.queryForList(
                "SELECT * FROM reminders WHERE pet_id = ? AND type = 'VACCINATION'", petId
        );
        assertEquals(1, reminders.size(), "An automated reminder must be inserted in reminders table");
        Map<String, Object> reminder = reminders.get(0);
        assertEquals("VACCINATION", reminder.get("type"));
        assertEquals(Boolean.FALSE, reminder.get("sent"));
        assertEquals(java.sql.Date.valueOf(nextDue).toString(), reminder.get("due_date").toString());
    }

    @Test
    @DisplayName("Should fetch unified chronological timeline sorted by date descending")
    void testGetPetHistoryChronologicalTimeline() {
        // Record 1: Earlier medical visit
        MedicalRecord med1 = new MedicalRecord(null, petId, vetId, null, LocalDate.now().minusMonths(6), "Puppy checkup", "Multivitamins");
        recordService.addMedicalRecord(med1, vetId);

        // Record 2: Vaccination
        VaccinationRecord vac1 = new VaccinationRecord(null, petId, "DHPP Vaccine", LocalDate.now().minusMonths(2), LocalDate.now().plusMonths(10));
        recordService.addVaccination(vac1);

        // Record 3: Recent medical visit
        MedicalRecord med2 = new MedicalRecord(null, petId, vetId, null, LocalDate.now(), "Ear infection", "Otomax drops");
        recordService.addMedicalRecord(med2, vetId);

        List<PetMedicalTimelineDTO> timeline = recordService.getPetHistory(petId);

        assertEquals(3, timeline.size());
        // Most recent first:
        assertEquals("MEDICAL", timeline.get(0).getType());
        assertEquals("Ear infection", timeline.get(0).getTitle());

        assertEquals("VACCINATION", timeline.get(1).getType());
        assertEquals("DHPP Vaccine", timeline.get(1).getTitle());

        assertEquals("MEDICAL", timeline.get(2).getType());
        assertEquals("Puppy checkup", timeline.get(2).getTitle());
    }
}

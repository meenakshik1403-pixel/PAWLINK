package com.pawlink.reminder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pawlink.pet.PetDAO;
import com.pawlink.pet.PetFactory;
import com.pawlink.reminder.dto.ReminderRequest;
import com.pawlink.user.UserDAO;
import com.pawlink.user.UserFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ReminderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private PetDAO petDAO;

    @Autowired
    private ReminderDAO reminderDAO;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long ownerId;
    private Long petId;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM appointments");
        jdbcTemplate.update("DELETE FROM reminders");
        jdbcTemplate.update("DELETE FROM vaccinations");
        jdbcTemplate.update("DELETE FROM medical_records");
        jdbcTemplate.update("DELETE FROM pets");
        jdbcTemplate.update("DELETE FROM users");

        var owner = UserFactory.createUser("OWNER", null, "John Doe", "john@pawlink.com", "+1-555-0202", "hash", LocalDateTime.now());
        userDAO.insert(owner);
        ownerId = owner.getUserId();

        var pet = PetFactory.createPet("Cat", "Luna", LocalDate.of(2023, 1, 15), ownerId);
        petDAO.insert(pet);
        petId = pet.getPetId();
    }

    @Test
    @DisplayName("POST /api/reminders creates new scheduled reminder and returns 201 Created")
    void testCreateReminder() throws Exception {
        ReminderRequest req = new ReminderRequest(petId, "MEDICATION", LocalDate.now().plusDays(3));

        mockMvc.perform(post("/api/reminders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", ownerId)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.petName", is("Luna")))
                .andExpect(jsonPath("$.type", is("MEDICATION")))
                .andExpect(jsonPath("$.sent", is(false)))
                .andExpect(jsonPath("$.urgency", is("UPCOMING")));
    }

    @Test
    @DisplayName("GET /api/reminders?due=true returns only due & unsent reminders")
    void testGetDueReminders() throws Exception {
        // Due reminder
        Reminder due = new Reminder(petId, "VACCINATION", LocalDate.now());
        reminderDAO.insert(due);

        // Future reminder
        Reminder future = new Reminder(petId, "CHECKUP", LocalDate.now().plusDays(7));
        reminderDAO.insert(future);

        mockMvc.perform(get("/api/reminders?due=true")
                        .header("X-User-Id", ownerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].type", is("VACCINATION")))
                .andExpect(jsonPath("$[0].petName", is("Luna")));
    }

    @Test
    @DisplayName("POST /api/reminders/run executes observer engine and returns processed count")
    void testRunRemindersEngine() throws Exception {
        // Seed 2 due reminders
        reminderDAO.insert(new Reminder(petId, "VACCINATION", LocalDate.now().minusDays(2)));
        reminderDAO.insert(new Reminder(petId, "MEDICATION", LocalDate.now()));

        mockMvc.perform(post("/api/reminders/run"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.processedCount", is(2)))
                .andExpect(jsonPath("$.message", containsString("Processed and dispatched 2 due reminder(s)")));

        // Calling run again should process 0
        mockMvc.perform(post("/api/reminders/run"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.processedCount", is(0)));
    }
}


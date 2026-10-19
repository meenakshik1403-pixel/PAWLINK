package com.pawlink.medical;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller exposing Medical and Vaccination record endpoints:
 * - GET /api/pets/{id}/records
 * - POST /api/pets/{id}/medical
 * - POST /api/pets/{id}/vaccinations
 */
@RestController
@RequestMapping("/api/pets/{id}")
public class MedicalController {

    private final RecordService recordService;

    public MedicalController(RecordService recordService) {
        this.recordService = recordService;
    }

    /**
     * GET /api/pets/{id}/records: Returns the unified chronological clinical timeline
     * combining medical visits and vaccinations, sorted by date descending.
     */
    @GetMapping("/records")
    public ResponseEntity<List<PetMedicalTimelineDTO>> getPetHistory(
            @PathVariable("id") Long petId,
            @RequestHeader(name = "X-User-Id", required = false) Long xUserId
    ) {
        List<PetMedicalTimelineDTO> timeline = recordService.getPetHistory(petId);
        return ResponseEntity.ok(timeline);
    }

    /**
     * POST /api/pets/{id}/medical: Records a clinical examination and prescription.
     */
    @PostMapping("/medical")
    public ResponseEntity<MedicalRecord> addMedicalRecord(
            @PathVariable("id") Long petId,
            @RequestBody MedicalRecord record,
            @RequestHeader(name = "X-User-Id", required = false) Long xUserId
    ) {
        record.setPetId(petId);
        if (record.getVetId() == null) {
            record.setVetId(xUserId);
        }
        MedicalRecord created = recordService.addMedicalRecord(record, xUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * POST /api/pets/{id}/vaccinations: Records a vaccination and automatically
     * triggers the insertion of a vaccination reminder in the reminders table.
     */
    @PostMapping("/vaccinations")
    public ResponseEntity<VaccinationRecord> addVaccination(
            @PathVariable("id") Long petId,
            @RequestBody VaccinationRecord vac,
            @RequestHeader(name = "X-User-Id", required = false) Long xUserId
    ) {
        vac.setPetId(petId);
        VaccinationRecord created = recordService.addVaccination(vac);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * GET /api/pets/{id}/medical: Lists all medical consultations for the pet.
     */
    @GetMapping("/medical")
    public ResponseEntity<List<MedicalRecord>> getMedicalRecords(@PathVariable("id") Long petId) {
        List<MedicalRecord> list = recordService.getMedicalRecords(petId);
        return ResponseEntity.ok(list);
    }

    /**
     * GET /api/pets/{id}/vaccinations: Lists all administered vaccinations for the pet.
     */
    @GetMapping("/vaccinations")
    public ResponseEntity<List<VaccinationRecord>> getVaccinations(@PathVariable("id") Long petId) {
        List<VaccinationRecord> list = recordService.getVaccinations(petId);
        return ResponseEntity.ok(list);
    }
}

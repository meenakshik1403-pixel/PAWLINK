package com.pawlink.web.controller;

import com.pawlink.web.api.ApiGateway;
import com.pawlink.web.dto.AuthUserDTO;
import com.pawlink.web.dto.SitterDTO;
import com.pawlink.web.dto.SystemStatsDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AdminWebControllerTest {

    private FakeApiGateway fakeApiGateway;
    private AdminWebController controller;
    private MockHttpSession adminSession;
    private MockHttpSession ownerSession;

    static class FakeApiGateway extends ApiGateway {
        Object mockGetResponse;
        String lastPatchUri;

        public FakeApiGateway() {
            super();
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T get(String uri, Class<T> responseType) {
            return (T) mockGetResponse;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T patch(String uri, Object body, Class<T> responseType) {
            this.lastPatchUri = uri;
            return null;
        }
    }

    @BeforeEach
    void setUp() {
        fakeApiGateway = new FakeApiGateway();
        controller = new AdminWebController(fakeApiGateway);

        adminSession = new MockHttpSession();
        adminSession.setAttribute("user", new AuthUserDTO(1L, "Admin User", "admin@pawlink.com", "ADMIN"));

        ownerSession = new MockHttpSession();
        ownerSession.setAttribute("user", new AuthUserDTO(2L, "Pet Owner", "owner@pawlink.com", "OWNER"));
    }

    @Test
    @DisplayName("listSitters redirects to login when session user is null")
    void testListSittersNoSession() {
        Model model = new ConcurrentModel();
        MockHttpSession emptySession = new MockHttpSession();

        String view = controller.listSitters(model, emptySession);

        assertEquals("redirect:/login", view);
    }

    @Test
    @DisplayName("listSitters redirects to home when user role is not ADMIN")
    void testListSittersNonAdmin() {
        Model model = new ConcurrentModel();

        String view = controller.listSitters(model, ownerSession);

        assertEquals("redirect:/?error=Admin+access+required", view);
    }

    @Test
    @DisplayName("listSitters populates sitters model attribute and returns admin/sitters view")
    void testListSittersAdminSuccess() {
        Model model = new ConcurrentModel();
        SitterDTO[] sitters = new SitterDTO[]{
                new SitterDTO(10L, "Alice Sitter", "alice@example.com", "555-0101", new BigDecimal("30.00"), false)
        };
        fakeApiGateway.mockGetResponse = sitters;

        String view = controller.listSitters(model, adminSession);

        assertEquals("admin/sitters", view);
        assertTrue(model.containsAttribute("sitters"));
        @SuppressWarnings("unchecked")
        List<SitterDTO> list = (List<SitterDTO>) model.getAttribute("sitters");
        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals("Alice Sitter", list.get(0).getName());
    }

    @Test
    @DisplayName("approveSitter executes PATCH and redirects to /admin/sitters with success flash attribute")
    void testApproveSitterSuccess() {
        RedirectAttributesModelMap redirectAttrs = new RedirectAttributesModelMap();

        String view = controller.approveSitter(10L, adminSession, redirectAttrs);

        assertEquals("redirect:/admin/sitters", view);
        assertEquals("/api/admin/sitters/10/approve", fakeApiGateway.lastPatchUri);
        assertTrue(redirectAttrs.getFlashAttributes().containsKey("success"));
    }

    @Test
    @DisplayName("approveSitter redirects home if non-admin attempts approval")
    void testApproveSitterNonAdmin() {
        RedirectAttributesModelMap redirectAttrs = new RedirectAttributesModelMap();

        String view = controller.approveSitter(10L, ownerSession, redirectAttrs);

        assertEquals("redirect:/", view);
        assertNull(fakeApiGateway.lastPatchUri);
        assertTrue(redirectAttrs.getFlashAttributes().containsKey("error"));
    }

    @Test
    @DisplayName("viewStats populates stats model attribute and returns admin/stats view")
    void testViewStatsAdminSuccess() {
        Model model = new ConcurrentModel();
        SystemStatsDTO mockStats = new SystemStatsDTO(10, 5, 3,
                Map.of("OWNER", 6, "VET", 2, "SITTER", 1, "ADMIN", 1),
                Map.of("Dr. Vet", 5),
                Map.of("Alice Sitter", 3));
        fakeApiGateway.mockGetResponse = mockStats;

        String view = controller.viewStats(model, adminSession);

        assertEquals("admin/stats", view);
        assertTrue(model.containsAttribute("stats"));
        SystemStatsDTO stats = (SystemStatsDTO) model.getAttribute("stats");
        assertNotNull(stats);
        assertEquals(10, stats.getTotalUsers());
        assertEquals(5, stats.getTotalAppointments());
        assertEquals(3, stats.getTotalBookings());
    }

    @Test
    @DisplayName("viewStats redirects home if unauthorized user role")
    void testViewStatsUnauthorizedRole() {
        Model model = new ConcurrentModel();

        String view = controller.viewStats(model, ownerSession);

        assertEquals("redirect:/?error=Admin+or+Vet+access+required", view);
    }
}


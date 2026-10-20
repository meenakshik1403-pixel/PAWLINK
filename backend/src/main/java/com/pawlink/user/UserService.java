package com.pawlink.user;

import com.pawlink.common.exception.AccountPendingApprovalException;
import com.pawlink.common.exception.AuthenticationFailedException;
import com.pawlink.common.exception.ResourceNotFoundException;
import com.pawlink.common.exception.UserAlreadyExistsException;
import com.pawlink.sitter.SitterDAO;
import com.pawlink.user.dto.AuthResponse;
import com.pawlink.user.dto.LoginRequest;
import com.pawlink.user.dto.RegisterRequest;
import com.pawlink.user.dto.UserSummaryDTO;
import com.pawlink.vet.VetDAO;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service Layer for User management and authentication.
 * Handles BCrypt password hashing, credential verification, and user queries.
 * Enforces admin verification invariant: Unapproved Sitters and Vets cannot log in.
 */
@Service
public class UserService {

    private final UserDAO userDAO;
    private final PasswordEncoder passwordEncoder;
    private final SitterDAO sitterDAO;
    private final VetDAO vetDAO;

    public UserService(UserDAO userDAO, PasswordEncoder passwordEncoder, SitterDAO sitterDAO, VetDAO vetDAO) {
        this.userDAO = userDAO;
        this.passwordEncoder = passwordEncoder;
        this.sitterDAO = sitterDAO;
        this.vetDAO = vetDAO;
    }

    /**
     * Registers a new user with BCrypt password hashing.
     *
     * @param req Registration payload
     * @return AuthResponse with new user's ID, name, email, and role
     * @throws UserAlreadyExistsException if email is already taken
     */
    public AuthResponse register(RegisterRequest req) {
        if ("ADMIN".equalsIgnoreCase(req.getRole())) {
            throw new IllegalArgumentException("Admin accounts cannot be registered publicly. Admin credentials must be configured via environment variables.");
        }
        String cleanEmail = req.getEmail().trim().toLowerCase();

        if (userDAO.existsByEmail(cleanEmail)) {
            throw new UserAlreadyExistsException("Email address '" + cleanEmail + "' is already registered.");
        }

        // Secure password hashing with BCrypt
        String hashedPassword = passwordEncoder.encode(req.getPassword());

        // Polymorphic creation via UserFactory
        User newUser = UserFactory.createUser(
                req.getRole(),
                req.getName().trim(),
                cleanEmail,
                req.getPhone() != null ? req.getPhone().trim() : null,
                hashedPassword
        );

        User savedUser = userDAO.insert(newUser);

        // Initialize unverified status for Sitters and Vets pending admin approval
        if ("SITTER".equalsIgnoreCase(savedUser.getRole())) {
            sitterDAO.saveSitter(savedUser.getUserId(), BigDecimal.valueOf(250.0), false);
        } else if ("VET".equalsIgnoreCase(savedUser.getRole())) {
            vetDAO.saveVet(savedUser.getUserId(), false);
        }

        return new AuthResponse(
                savedUser.getUserId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getRole()
        );
    }

    /**
     * Authenticates a user against stored BCrypt password hash.
     * Enforces approval checks for Sitters and Vets.
     *
     * @param req Login payload
     * @return AuthResponse upon successful authentication
     * @throws AuthenticationFailedException if email not found or password mismatch
     * @throws AccountPendingApprovalException if Sitter or Vet is not yet approved by an Admin
     */
    public AuthResponse login(LoginRequest req) {
        String cleanEmail = req.getEmail().trim().toLowerCase();

        User user = userDAO.findByEmail(cleanEmail)
                .orElseThrow(() -> new AuthenticationFailedException("Invalid email or password."));

        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new AuthenticationFailedException("Invalid email or password.");
        }

        // Enforce admin verification invariant for Sitter and Vet logins
        if ("SITTER".equalsIgnoreCase(user.getRole())) {
            if (!sitterDAO.isVerified(user.getUserId())) {
                throw new AccountPendingApprovalException("Your Pet Sitter account is pending admin approval. Please wait for an administrator to verify your profile before logging in.");
            }
        } else if ("VET".equalsIgnoreCase(user.getRole())) {
            if (!vetDAO.isVerified(user.getUserId())) {
                throw new AccountPendingApprovalException("Your Veterinarian account is pending admin approval. Please wait for an administrator to verify your credentials before logging in.");
            }
        }

        return new AuthResponse(
                user.getUserId(),
                user.getName(),
                user.getEmail(),
                user.getRole()
        );
    }

    /**
     * Retrieves all registered veterinarians.
     *
     * @return list of UserSummaryDTOs with VET role
     */
    public List<UserSummaryDTO> getVeterinarians() {
        return userDAO.findVets().stream()
                .map(u -> new UserSummaryDTO(
                        u.getUserId(),
                        u.getName(),
                        u.getEmail(),
                        u.getPhone(),
                        u.getRole()
                ))
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a user by their ID.
     *
     * @param id user ID
     * @return concrete User instance
     * @throws ResourceNotFoundException if user ID does not exist
     */
    public User getUserById(Long id) {
        return userDAO.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
    }
}

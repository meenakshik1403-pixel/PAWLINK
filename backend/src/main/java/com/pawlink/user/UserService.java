package com.pawlink.user;

import com.pawlink.common.exception.AuthenticationFailedException;
import com.pawlink.common.exception.ResourceNotFoundException;
import com.pawlink.common.exception.UserAlreadyExistsException;
import com.pawlink.user.dto.AuthResponse;
import com.pawlink.user.dto.LoginRequest;
import com.pawlink.user.dto.RegisterRequest;
import com.pawlink.user.dto.UserSummaryDTO;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service Layer for User management and authentication.
 * Handles BCrypt password hashing, credential verification, and user queries.
 */
@Service
public class UserService {

    private final UserDAO userDAO;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserDAO userDAO, PasswordEncoder passwordEncoder) {
        this.userDAO = userDAO;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registers a new user with BCrypt password hashing.
     *
     * @param req Registration payload
     * @return AuthResponse with new user's ID, name, email, and role
     * @throws UserAlreadyExistsException if email is already taken
     */
    public AuthResponse register(RegisterRequest req) {
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

        return new AuthResponse(
                savedUser.getUserId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getRole()
        );
    }

    /**
     * Authenticates a user against stored BCrypt password hash.
     *
     * @param req Login payload
     * @return AuthResponse upon successful authentication
     * @throws AuthenticationFailedException if email not found or password mismatch
     */
    public AuthResponse login(LoginRequest req) {
        String cleanEmail = req.getEmail().trim().toLowerCase();

        User user = userDAO.findByEmail(cleanEmail)
                .orElseThrow(() -> new AuthenticationFailedException("Invalid email or password."));

        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new AuthenticationFailedException("Invalid email or password.");
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
     * @return User domain object
     * @throws ResourceNotFoundException if user not found
     */
    public User getUserById(Long id) {
        return userDAO.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }
}

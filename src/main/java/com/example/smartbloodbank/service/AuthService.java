package com.example.smartbloodbank.service;

import com.example.smartbloodbank.model.CampaignOrganizer;
import com.example.smartbloodbank.model.Donor;
import com.example.smartbloodbank.model.HospitalStaff;
import com.example.smartbloodbank.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

public class AuthService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthService.class);
    private final Firestore db = FirestoreService.getDb();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    private final String FIREBASE_API_KEY = System.getenv("FIREBASE_WEB_API_KEY");
    private final String AUTH_URL = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=" + FIREBASE_API_KEY;

    public UserRecord registerUser(Map<String, Object> userData) throws FirebaseAuthException {
        String email = (String) userData.get("email");
        String password = (String) userData.get("password");

        UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                .setEmail(email)
                .setPassword(password)
                .setDisplayName((String) userData.get("username"));

        UserRecord userRecord = auth.createUser(request);
        LOGGER.info("Successfully created new user in Firebase Auth: {}", userRecord.getUid());

        userData.remove("password");
        db.collection("users").document(userRecord.getUid()).set(userData);
        LOGGER.info("Successfully created user profile in Firestore for UID: {}", userRecord.getUid());
        return userRecord;
    }

    public boolean usernameExists(String username) throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = db.collection("users").whereEqualTo("username", username).limit(1).get();
        return !future.get().isEmpty();
    }

    public String login(String identifier, String password) throws ExecutionException, InterruptedException {
        String email = identifier;
        // If the identifier doesn't look like an email, assume it's a username
        if (!EMAIL_PATTERN.matcher(identifier).matches()) {
            LOGGER.debug("Identifier '{}' is not an email, looking up username in Firestore.", identifier);
            ApiFuture<QuerySnapshot> future = db.collection("users").whereEqualTo("username", identifier).limit(1).get();
            QuerySnapshot querySnapshot = future.get();
            if (querySnapshot.isEmpty()) {
                LOGGER.warn("Login failed: Username '{}' not found.", identifier);
                return null; // Username not found
            }
            email = querySnapshot.getDocuments().get(0).getString("email");
            LOGGER.debug("Found email '{}' for username '{}'.", email, identifier);
        }
        return loginWithEmailPassword(email, password);
    }

    private String loginWithEmailPassword(String email, String password) {
        try {
            Map<String, Object> requestBody = Map.of("email", email, "password", password, "returnSecureToken", true);
            String jsonPayload = objectMapper.writeValueAsString(requestBody);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(AUTH_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                Map<String, String> responseMap = objectMapper.readValue(response.body(), Map.class);
                return responseMap.get("localId");
            } else {
                return null;
            }
        } catch (IOException | InterruptedException e) {
            LOGGER.error("Exception during REST API login", e);
            Thread.currentThread().interrupt();
            return null;
        }
    }

    public User getUserProfile(String uid) {
        try {
            ApiFuture<DocumentSnapshot> future = db.collection("users").document(uid).get();
            DocumentSnapshot document = future.get();
            if (document.exists()) {
                LOGGER.info("Successfully fetched profile for UID: {}", uid);

                String role = document.getString("role");
                if (role == null) {
                    LOGGER.error("CRITICAL: User document for UID {} is missing 'role' field.", uid);
                    return document.toObject(User.class);
                }

                User user;
                switch (role) {
                    case "Donor":
                        user = document.toObject(Donor.class);
                        break;
                    case "Hospital Staff":
                    case "HospitalStaff":
                        user = document.toObject(HospitalStaff.class);
                        break;
                    case "Campaign Organizer":
                    case "CampaignOrganizer":
                        user = document.toObject(CampaignOrganizer.class);
                        break;
                    default:
                        LOGGER.warn("Unrecognized role '{}' for UID {}. Defaulting to base User.", role, uid);
                        user = document.toObject(User.class);
                        break;
                }

                if (user != null) {
                    user.setUid(document.getId());
                }
                return user;

            } else {
                LOGGER.error("CRITICAL: No profile document found in Firestore for authenticated UID: {}", uid);
                return null;
            }
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("Error fetching user profile for UID: {}", uid, e);
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return null;
        }
    }
}
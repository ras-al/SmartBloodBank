package com.example.smartbloodbank.controller;

import com.example.smartbloodbank.main.SceneManager;
import com.example.smartbloodbank.model.User;
import com.example.smartbloodbank.service.FirestoreService;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import java.io.IOException;
import java.util.Map;

public class DashboardController {
    @FXML
    private Button notificationsButton;
    @FXML
    private BorderPane mainPane;
    @FXML
    private Label welcomeLabel;
    @FXML
    private Button donorDashboardButton;
    @FXML
    private Button postRequestButton;
    @FXML
    private Button viewStatusButton;
    @FXML
    private Button partnerHospitalButton;
    @FXML
    private Button createCampaignButton;
    @FXML
    private Button manageCampaignsButton;

    // --- NEW BUTTON ---
    @FXML
    private Button aiSuggestionsButton;

    private User currentUser;

    public void initData(User user) {
        this.currentUser = user;
        welcomeLabel.setText("Welcome, " + user.getUsername() + "!");

        configureSidebar(user.getRole());
        setupNotificationListener(user);
        handleHomeButton();
    }

    private void configureSidebar(String role) {
        boolean isDonor = "Donor".equals(role);
        boolean isHospital = "HospitalStaff".equals(role) || "Hospital Staff".equals(role);
        boolean isOrganizer = "CampaignOrganizer".equals(role) || "Campaign Organizer".equals(role);

        donorDashboardButton.setVisible(isDonor);
        donorDashboardButton.setManaged(isDonor);

        postRequestButton.setVisible(isHospital);
        postRequestButton.setManaged(isHospital);

        viewStatusButton.setVisible(isHospital);
        viewStatusButton.setManaged(isHospital);

        createCampaignButton.setVisible(isOrganizer);
        createCampaignButton.setManaged(isOrganizer);

        manageCampaignsButton.setVisible(isOrganizer);
        manageCampaignsButton.setManaged(isOrganizer);

        aiSuggestionsButton.setVisible(isOrganizer);
        aiSuggestionsButton.setManaged(isOrganizer);

        partnerHospitalButton.setVisible(isHospital);
        partnerHospitalButton.setManaged(isHospital);

        notificationsButton.setVisible(isDonor);
        notificationsButton.setManaged(isDonor);
    }

    private void setupNotificationListener(User user) {
        if (!"Donor".equals(user.getRole())) {
            return;
        }

        Firestore db = FirestoreService.getDb();
        db.collection("notifications")
                .whereEqualTo("donorId", user.getUid())
                .whereEqualTo("status", "unread")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        System.err.println("Notification listener failed: " + e);
                        return;
                    }

                    if (snapshots != null && !snapshots.isEmpty()) {
                        for (QueryDocumentSnapshot doc : snapshots) {
                            String message = doc.getString("message");
                            Platform.runLater(() -> {
                                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                alert.setTitle("Urgent Blood Request!");
                                alert.setHeaderText("You are a potential match!");
                                alert.setContentText(message + "\nPlease check your notifications panel to respond.");
                                alert.showAndWait();
                            });
                        }
                    }
                });
    }

    @FXML
    protected void handleHomeButton() {
        if (currentUser != null && ("HospitalStaff".equals(currentUser.getRole()) || "Hospital Staff".equals(currentUser.getRole()))) {
            loadView("/com/example/smartbloodbank/HospitalHomePageView.fxml");
        } else if (currentUser != null && ("CampaignOrganizer".equals(currentUser.getRole()) || "Campaign Organizer".equals(currentUser.getRole()))) {
            loadView("/com/example/smartbloodbank/CampaignOrganizerHomePageView.fxml");
        } else {
            loadView("/com/example/smartbloodbank/HomePageView.fxml");
        }
    }

    @FXML
    protected void handleProfileButton() {
        loadView("/com/example/smartbloodbank/ProfileView.fxml");
    }

    @FXML
    protected void handleNotificationsButton() {
        loadView("/com/example/smartbloodbank/NotificationsView.fxml");
    }

    @FXML
    protected void handlePartnerHospitalButton() {
        loadView("/com/example/smartbloodbank/PartnerHospitalView.fxml");
    }

    @FXML
    protected void handleDonorDashboardButton() {
        loadView("/com/example/smartbloodbank/DonorDashboardView.fxml");
    }

    @FXML
    protected void handlePostRequestButton() {
        loadView("/com/example/smartbloodbank/PostRequestView.fxml");
    }

    @FXML
    protected void handleViewStatusButton() {
        loadView("/com/example/smartbloodbank/RequestStatusView.fxml");
    }

    @FXML
    protected void handleAiSuggestionsButton() {
        loadView("/com/example/smartbloodbank/AiSuggestionsView.fxml");
    }

    @FXML
    protected void handleCreateCampaignButton() {
        loadView("/com/example/smartbloodbank/CreateCampaignView.fxml");
    }

    @FXML
    protected void handleManageCampaignsButton() {
        loadView("/com/example/smartbloodbank/ManageCampaignsView.fxml");
    }

    public void loadView(String fxmlFile, Object data) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent subView = loader.load();

            if (fxmlFile.contains("CreateCampaignView") && data instanceof Map) {
                CreateCampaignController controller = loader.getController();
                controller.initData(currentUser.getUid(), (Map<String, Object>) data);
            } else {
                loadViewInternal(loader, fxmlFile, subView);
            }

            mainPane.setCenter(subView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadView(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent subView = loader.load();
            loadViewInternal(loader, fxmlFile, subView);
            mainPane.setCenter(subView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadViewInternal(FXMLLoader loader, String fxmlFile, Parent subView) {
        if (fxmlFile.contains("DonorDashboardView")) {
            DonorDashboardController controller = loader.getController();
            controller.initData(currentUser);
        } else if (fxmlFile.contains("HospitalHomePageView")) {
            HospitalHomePageController controller = loader.getController();
            controller.setDashboardController(this);
            controller.initData(currentUser);
        } else if (fxmlFile.contains("CampaignOrganizerHomePageView")) {
            CampaignOrganizerHomePageController controller = loader.getController();
            controller.setDashboardController(this);
            controller.initData(currentUser);
        } else if (fxmlFile.contains("HomePageView")) {
            HomePageController controller = loader.getController();
            controller.setDashboardController(this);
            controller.initData(currentUser);
        } else if (fxmlFile.contains("ProfileView")) {
            ProfileController controller = loader.getController();
            controller.initData(currentUser);
        } else if (fxmlFile.contains("CreateCampaignView")) {
            CreateCampaignController controller = loader.getController();
            controller.initData(currentUser.getUid()); // Standard init
        } else if (fxmlFile.contains("ManageCampaignsView")) {
            ManageCampaignsController controller = loader.getController();
            controller.initData(currentUser.getUid());
        } else if (fxmlFile.contains("NotificationsView")) {
            NotificationsController controller = loader.getController();
            controller.initData(currentUser);
        } else if (fxmlFile.contains("AiSuggestionsView")) {
            AiSuggestionsController controller = loader.getController();
            controller.setDashboardController(this);
        } else if (fxmlFile.contains("FindDriveView")) {
        }
    }

    @FXML
    protected void handleLogout() {
        try {
            SceneManager.getInstance().switchToScene("/com/example/smartbloodbank/LandingPageView.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
package com.rensights.admin.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired(required = false)
    private MicrosoftGraphEmailService graphEmailService;

    @Value("${app.email.enabled:true}")
    private boolean emailEnabled;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    /**
     * Notify a user that their requested property analysis report is ready to view.
     */
    public void sendReportReadyEmail(String toEmail, String propertyLabel, String requestId) {
        logger.info("=== EmailService.sendReportReadyEmail called ===");
        logger.info("To email: {}, requestId: {}", toEmail, requestId);

        if (toEmail == null || toEmail.isBlank()) {
            logger.warn("No email address on analysis request {} - skipping report-ready email", requestId);
            return;
        }

        if (!emailEnabled) {
            logger.warn("Email is disabled. Report-ready notification for {}: Request ID {}", toEmail, requestId);
            return;
        }

        String reportUrl = frontendUrl + "/analysis-request?id=" + requestId;
        String subject = "Rensights - Your Property Report Is Ready";
        String body = String.join("\n",
            "Good news! Your requested property analysis report is ready.",
            "",
            "Property: " + (propertyLabel != null && !propertyLabel.isBlank() ? propertyLabel : "Your requested property"),
            "",
            "View your full report here:",
            reportUrl,
            "",
            "Thank you for using Rensights.",
            "",
            "Best regards,",
            "Rensights Team"
        );

        if (graphEmailService == null) {
            logger.error("Microsoft Graph API is not configured! Report-ready email cannot be sent.");
            logger.warn("DEV MODE: Report Ready - To: {}, Request ID: {}, URL: {}", toEmail, requestId, reportUrl);
            return;
        }

        try {
            graphEmailService.sendEmail(toEmail, subject, body);
            logger.info("Report-ready email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            logger.error("Failed to send report-ready email to: {}", toEmail, e);
            // Don't throw - email failure shouldn't block the admin's status update
        }
    }
}

package tech.thedumbdev.shop_lens.service.impl;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tech.thedumbdev.shop_lens.service.EmailService;

@Service
public class EmailServiceImpl implements EmailService {

    private final String fromEmail;
    private final Resend resend;

    EmailServiceImpl(
            @Value("${resend.api.key}") String apiKey,
            @Value("${resend.from.email}") String fromEmail
) {
        this.resend = new Resend(apiKey);
        this.fromEmail = fromEmail;
    }

    @Override
    public void sendOtpEmail(String toEmail, String otp) {

        String htmlContent = """
            <div style="font-family: Arial, sans-serif; max-width: 480px; margin: auto; padding: 20px; border: 1px solid #e5e7eb; border-radius: 8px; background-color: #ffffff;">
                <h2 style="color: #111827; text-align: center; margin-bottom: 20px;">Your Verification Code</h2>
                <p style="font-size: 16px; color: #374151;">Hi there,</p>
                <p style="font-size: 16px; color: #374151;">Use the verification code below to continue your login process:</p>
                <div style="margin: 24px 0; text-align: center;">
                    <span style="font-size: 28px; font-weight: bold; letter-spacing: 4px; padding: 12px 24px; display: inline-block; background-color: #f3f4f6; border-radius: 6px; color: #111827;">
                        %s
                    </span>
                </div>
                <p style="font-size: 14px; color: #6b7280;">This code is valid for the next <strong>5 minutes</strong>.</p>
                <p style="font-size: 14px; color: #6b7280; margin-top: 32px; text-align: center;">If you did not request this, you can safely ignore this email.</p>
                <p style="font-size: 12px; color: #9ca3af; text-align: center; margin-top: 24px;">© 2025 Shop Lens. All rights reserved.</p>
            </div>
            """.formatted(otp);

        CreateEmailOptions params = CreateEmailOptions.builder()
                .from("Shop Lens <" + fromEmail + ">")
                .to(toEmail)
                .subject("Your OTP Code for shop-lens")
                .html(htmlContent)
                .build();

        try {
            resend.emails().send(params);
        } catch (ResendException e) {
            System.err.println("Error sending email: " + e.getMessage());
            throw new RuntimeException("Failed to send email to " + toEmail + "\n" + e.getMessage());
        }
    }
}

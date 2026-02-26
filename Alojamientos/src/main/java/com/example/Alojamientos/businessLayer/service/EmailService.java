package com.example.Alojamientos.businessLayer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void enviarCodigoRecuperacion(String destinatario, String codigo) {
        try {
            log.info("Intentando enviar email a: {}", destinatario);
            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setTo(destinatario);
            mensaje.setSubject("Hosped - Código de recuperación de contraseña");
            mensaje.setText(
                    "Hola,\n\n" +
                            "Tu código de verificación es:\n\n" +
                            "    " + codigo + "\n\n" +
                            "Este código expira en 15 minutos.\n\n" +
                            "© 2025 Hosped"
            );
            mailSender.send(mensaje);
            log.info("Email enviado exitosamente a: {}", destinatario);
        } catch (Exception e) {
            log.error("ERROR al enviar email a {}: {}", destinatario, e.getMessage(), e);
            throw new RuntimeException("No se pudo enviar el email: " + e.getMessage(), e);
        }
    }
}
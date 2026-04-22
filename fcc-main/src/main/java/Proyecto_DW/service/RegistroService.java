/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Proyecto_DW.service;

import Proyecto_DW.domain.Usuario;
import jakarta.mail.MessagingException;
import java.util.Locale;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 * @author Alfaro
 */
@Service
public class RegistroService {

    private final CorreoService correoService;
    private final UsuarioService usuarioService;
    private final MessageSource messageSource;

    public RegistroService(CorreoService correoService, UsuarioService usuarioService, MessageSource messageSource) {
        this.correoService = correoService;
        this.usuarioService = usuarioService;
        this.messageSource = messageSource;
    }

    //Este método se usa en el enlace del correo enviado...
    public Model activar(Model model, String nombre, String clave) {
        Optional<Usuario> usuario = usuarioService.getUsuarioByEmail(clave);
        if (!usuario.isEmpty()) {  //Si estaba...
            model.addAttribute("usuario", usuario.get());
        } else { //hay que devolver error
            model.addAttribute("titulo", messageSource.getMessage("registro.activar", null, Locale.getDefault()));
            model.addAttribute("mensaje", messageSource.getMessage("registro.activar.error", null, Locale.getDefault()));
        }
        return model;
    }

    //Este método es el que finalmente crea el usuario en el sistema
    public void activar(Usuario usuario, MultipartFile imagenFile) {
        usuario.setActivo(true);
        usuarioService.save(usuario);
    }

    public Model crearUsuario(Model model, Usuario usuario) throws MessagingException {
        String mensaje;
        try {
            String clave = demeClave();
            usuario.setPassword(clave);
            usuario.setActivo(false);
            usuarioService.save(usuario);
            enviaCorreoActivar(usuario, clave);
            mensaje = String.format(messageSource.getMessage("registro.mensaje.activacion.ok", null, Locale.getDefault()), usuario.getEmail());
        } catch (MessagingException | NoSuchMessageException e) {
            mensaje = String.format(messageSource.getMessage("registro.mensaje.usuario.o.correo", null, Locale.getDefault()), usuario.getNombre(), usuario.getEmail());
        }
        model.addAttribute("titulo", messageSource.getMessage("registro.activar", null, Locale.getDefault()));
        model.addAttribute("mensaje", mensaje);
        return model;
    }

    /*public Model recordarUsuario(Model model, Usuario usuario)
            throws MessagingException {
        String mensaje;
        Optional<Usuario> usuarioOpt = usuarioService.getUsuarioByEmail(mensaje);
        if (!usuarioOpt.isEmpty()) {
            usuario = usuarioOpt.get();
            String clave = demeClave();
            usuario.setPassword(clave);
            usuario.setActivo(false);
            usuarioService.save(usuario);
            enviaCorreoRecordar(usuario, clave);
            mensaje = String.format(messageSource.getMessage("registro.mensaje.recordar.ok", null, Locale.getDefault()), usuario.getEmail());
        } else {
            mensaje = String.format(messageSource.getMessage("registro.mensaje.usuario.o.correo", null, Locale.getDefault()), usuario.getNombre(), usuario.getEmail());
        }
        model.addAttribute("titulo", messageSource.getMessage("registro.activar", null, Locale.getDefault()));
        model.addAttribute("mensaje", mensaje);
        return model;
    }*/
    private String demeClave() {
        String tira = "ABCDEFGHIJKLMNOPQRSTUXYZabcdefghijklmnopqrstuvwxyz0123456789";
        String clave = "";
        for (int i = 0; i < 40; i++) {
            clave += tira.charAt((int) (Math.random() * tira.length()));
        }
        return clave;
    }

    //Ojo cómo le lee una informacion del application.properties
    @Value("${servidor.http}")
    private String servidor;

    private void enviaCorreoActivar(Usuario usuario, String clave) throws MessagingException {
        String mensaje = messageSource.getMessage("registro.correo.activar", null, Locale.getDefault());
        mensaje = String.format(mensaje, usuario.getNombre(), servidor, usuario.getNombre(), clave);
        String asunto = messageSource.getMessage("registro.mensaje.activacion", null, Locale.getDefault());
        correoService.enviarCorreoHtml(usuario.getEmail(), asunto, mensaje);
    }

    private void enviaCorreoRecordar(Usuario usuario, String clave) throws MessagingException {
        String mensaje = messageSource.getMessage("registro.correo.recordar", null, Locale.getDefault());
        mensaje = String.format(mensaje, usuario.getNombre(), servidor, usuario.getNombre(), clave);
        String asunto = messageSource.getMessage("registro.mensaje.recordar", null, Locale.getDefault());
        correoService.enviarCorreoHtml(usuario.getEmail(), asunto, mensaje);
    }

    private String construirCorreoBienvenida(String nombre, String email) {
        return """
            <!DOCTYPE html>
            <html lang="es">
            <head><meta charset="UTF-8"/><title>Bienvenido a ReservaCanchaCR</title></head>
            <body style="margin:0;padding:0;background-color:#f5f8f5;font-family:Arial,sans-serif;">
              <table width="100%%" cellpadding="0" cellspacing="0" style="background-color:#f5f8f5;padding:40px 0;">
                <tr><td align="center">
                  <table width="600" cellpadding="0" cellspacing="0"
                         style="background:#ffffff;border-radius:16px;overflow:hidden;
                                box-shadow:0 4px 24px rgba(0,0,0,0.08);">

                    <!-- HEADER -->
                    <tr>
                      <td style="background-color:#102210;padding:36px 40px;text-align:center;">
                        <div style="background-color:#0df20d;border-radius:12px;width:48px;height:48px;
                                    line-height:48px;font-size:28px;margin:0 auto 16px auto;
                                    text-align:center;">⚽</div>
                        <h1 style="margin:0;color:#0df20d;font-size:26px;font-weight:700;">
                          ReservaCanchaCR
                        </h1>
                        <p style="margin:6px 0 0 0;color:#a0b8a0;font-size:14px;">
                          Tu plataforma de reservas deportivas
                        </p>
                      </td>
                    </tr>

                    <!-- CUERPO -->
                    <tr>
                      <td style="padding:40px 40px 32px 40px;">
                        <h2 style="margin:0 0 8px 0;color:#102210;font-size:22px;font-weight:700;">
                          ¡Bienvenido, <span style="color:#0bc90b;">%s</span>! 🎉
                        </h2>
                        <p style="margin:0 0 24px 0;color:#4a5568;font-size:15px;line-height:1.6;">
                          Tu cuenta ha sido creada exitosamente en ReservaCanchaCR.
                          Ya podés iniciar sesión y empezar a reservar canchas.
                        </p>

                        <!-- TARJETA DATOS -->
                        <table width="100%%" cellpadding="0" cellspacing="0"
                               style="background:#f5f8f5;border-radius:12px;
                                      border-left:4px solid #0df20d;margin-bottom:28px;">
                          <tr>
                            <td style="padding:20px 24px;">
                              <p style="margin:0 0 8px 0;color:#102210;font-size:13px;
                                         font-weight:700;text-transform:uppercase;letter-spacing:1px;">
                                Datos de tu cuenta
                              </p>
                              <p style="margin:0;color:#4a5568;font-size:14px;line-height:1.9;">
                                📧 <strong>Correo:</strong> %s<br/>
                                👤 <strong>Nombre:</strong> %s<br/>
                                🔐 <strong>Estado:</strong> Cuenta activa
                              </p>
                            </td>
                          </tr>
                        </table>

                        <!-- ÍCONOS -->
                        <table width="100%%" cellpadding="0" cellspacing="0" style="margin-bottom:32px;">
                          <tr>
                            <td width="33%%" style="padding:0 8px 0 0;vertical-align:top;">
                              <table width="100%%" cellpadding="0" cellspacing="0"
                                     style="background:#f5f8f5;border-radius:10px;text-align:center;">
                                <tr><td style="padding:16px 12px;">
                                  <div style="font-size:26px;margin-bottom:8px;">🏟️</div>
                                  <p style="margin:0;color:#102210;font-size:13px;font-weight:600;">Ver canchas</p>
                                  <p style="margin:4px 0 0 0;color:#718096;font-size:12px;">Explorá las disponibles</p>
                                </td></tr>
                              </table>
                            </td>
                            <td width="33%%" style="padding:0 4px;vertical-align:top;">
                              <table width="100%%" cellpadding="0" cellspacing="0"
                                     style="background:#f5f8f5;border-radius:10px;text-align:center;">
                                <tr><td style="padding:16px 12px;">
                                  <div style="font-size:26px;margin-bottom:8px;">📅</div>
                                  <p style="margin:0;color:#102210;font-size:13px;font-weight:600;">Reservar</p>
                                  <p style="margin:4px 0 0 0;color:#718096;font-size:12px;">Elegí fecha y horario</p>
                                </td></tr>
                              </table>
                            </td>
                            <td width="33%%" style="padding:0 0 0 8px;vertical-align:top;">
                              <table width="100%%" cellpadding="0" cellspacing="0"
                                     style="background:#f5f8f5;border-radius:10px;text-align:center;">
                                <tr><td style="padding:16px 12px;">
                                  <div style="font-size:26px;margin-bottom:8px;">📋</div>
                                  <p style="margin:0;color:#102210;font-size:13px;font-weight:600;">Historial</p>
                                  <p style="margin:4px 0 0 0;color:#718096;font-size:12px;">Revisá tus reservas</p>
                                </td></tr>
                              </table>
                            </td>
                          </tr>
                        </table>

                        <!-- BOTÓN -->
                        <table cellpadding="0" cellspacing="0" style="margin:0 auto;">
                          <tr>
                            <td style="border-radius:10px;background-color:#0df20d;">
                              <a href="http://localhost:85/auth/login"
                                 style="display:inline-block;padding:14px 40px;color:#102210;
                                        font-size:15px;font-weight:700;text-decoration:none;
                                        border-radius:10px;">
                                Iniciar sesión →
                              </a>
                            </td>
                          </tr>
                        </table>
                      </td>
                    </tr>

                    <!-- FOOTER -->
                    <tr>
                      <td style="background-color:#102210;padding:24px 40px;text-align:center;">
                        <p style="margin:0 0 4px 0;color:#a0b8a0;font-size:12px;">
                          © 2025 ReservaCanchaCR · Todos los derechos reservados
                        </p>
                        <p style="margin:0;color:#5a7a5a;font-size:11px;">
                          Si no creaste esta cuenta, podés ignorar este correo.
                        </p>
                      </td>
                    </tr>

                  </table>
                </td></tr>
              </table>
            </body>
            </html>
            """.formatted(nombre, email, nombre);
    }

    private String construirCorreoRecuperacion(String nombre, String enlace) {
        return """
            <!DOCTYPE html>
            <html lang="es">
            <head><meta charset="UTF-8"/><title>Recuperación de contraseña</title></head>
            <body style="margin:0;padding:0;background-color:#f5f8f5;font-family:Arial,sans-serif;">
              <table width="100%%" cellpadding="0" cellspacing="0" style="background-color:#f5f8f5;padding:40px 0;">
                <tr><td align="center">
                  <table width="600" cellpadding="0" cellspacing="0"
                         style="background:#ffffff;border-radius:16px;overflow:hidden;
                                box-shadow:0 4px 24px rgba(0,0,0,0.08);">

                    <!-- HEADER -->
                    <tr>
                      <td style="background-color:#102210;padding:36px 40px;text-align:center;">
                        <div style="background-color:#0df20d;border-radius:12px;width:48px;height:48px;
                                    line-height:48px;font-size:28px;margin:0 auto 16px auto;
                                    text-align:center;">🔐</div>
                        <h1 style="margin:0;color:#0df20d;font-size:26px;font-weight:700;">
                          ReservaCanchaCR
                        </h1>
                        <p style="margin:6px 0 0 0;color:#a0b8a0;font-size:14px;">
                          Recuperación de contraseña
                        </p>
                      </td>
                    </tr>

                    <!-- CUERPO -->
                    <tr>
                      <td style="padding:40px 40px 32px 40px;">
                        <h2 style="margin:0 0 8px 0;color:#102210;font-size:22px;font-weight:700;">
                          Hola, <span style="color:#0bc90b;">%s</span>
                        </h2>
                        <p style="margin:0 0 24px 0;color:#4a5568;font-size:15px;line-height:1.6;">
                          Recibimos una solicitud para restablecer la contraseña de tu cuenta.
                          Hacé clic en el botón para acceder y cambiarla desde tu perfil.
                        </p>

                        <!-- AVISO -->
                        <table width="100%%" cellpadding="0" cellspacing="0" style="margin-bottom:28px;">
                          <tr>
                            <td style="background:#fff8e1;border-radius:12px;border-left:4px solid #f6c90e;
                                       padding:16px 20px;">
                              <p style="margin:0;color:#7a6000;font-size:13px;line-height:1.6;">
                                ⚠️ Si no solicitaste esto, podés ignorar este correo.
                                Tu contraseña no cambiará.
                              </p>
                            </td>
                          </tr>
                        </table>

                        <!-- BOTÓN -->
                        <table cellpadding="0" cellspacing="0" style="margin:0 auto;">
                          <tr>
                            <td style="border-radius:10px;background-color:#0df20d;">
                              <a href="%s"
                                 style="display:inline-block;padding:14px 40px;color:#102210;
                                        font-size:15px;font-weight:700;text-decoration:none;
                                        border-radius:10px;">
                                Ir al sitio →
                              </a>
                            </td>
                          </tr>
                        </table>
                      </td>
                    </tr>

                    <!-- FOOTER -->
                    <tr>
                      <td style="background-color:#102210;padding:24px 40px;text-align:center;">
                        <p style="margin:0 0 4px 0;color:#a0b8a0;font-size:12px;">
                          © 2025 ReservaCanchaCR · Todos los derechos reservados
                        </p>
                        <p style="margin:0;color:#5a7a5a;font-size:11px;">
                          Si no solicitaste esto, podés ignorar este correo.
                        </p>
                      </td>
                    </tr>

                  </table>
                </td></tr>
              </table>
            </body>
            </html>
            """.formatted(nombre, enlace);
    }
}

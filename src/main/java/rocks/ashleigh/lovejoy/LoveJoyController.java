package rocks.ashleigh.lovejoy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.view.RedirectView;
import rocks.ashleigh.lovejoy.datastructures.EvaluationRequest;
import rocks.ashleigh.lovejoy.datastructures.LoginForm;
import rocks.ashleigh.lovejoy.datastructures.RegistrationForm;
import rocks.ashleigh.lovejoy.jpa.EvaluationEntity;
import rocks.ashleigh.lovejoy.jpa.EvaluationRepository;
import rocks.ashleigh.lovejoy.jpa.UserEntity;
import rocks.ashleigh.lovejoy.jpa.UserRepository;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.util.Properties;
import java.util.Random;

@Controller
public class LoveJoyController {
    private UserRepository userRepo;
    private EvaluationRepository evalRepo;
    private Properties prop = new Properties();
    private Random random = new Random();
    private Session session;
    private String emailUser;

    public LoveJoyController(UserRepository userRepo, EvaluationRepository evalRepo, @Value("${EMAIL_USERNAME}") String emailUser, @Value("${EMAIL_PASSWORD}") String emailPass) {
        this.userRepo = userRepo;
        this.evalRepo = evalRepo;

        prop.put("mail.smtp.auth", true);
        prop.put("mail.smtp.starttls.enable", "true");
        prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.smtp.port", "465");
        prop.put("mail.smtp.ssl.trust", "smtp.gmail.com");
        this.emailUser = emailUser;

        session = Session.getInstance(prop, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(emailUser, emailPass);
            }
        });
    }

    // DONE
    @GetMapping("/")
    public String mainPage(Model model, HttpSession session) {
        model.addAttribute("login", session.getAttribute("login"));
        model.addAttribute("admin", session.getAttribute("admin"));
        return "main";
    }

    @GetMapping("/*")
    public RedirectView badURL() {
        return new RedirectView("/");
    }

    /** -- REGISTRATION -- **/
    @GetMapping("/register")
    public String registrationPage(Model model) {
        model.addAttribute("user", new RegistrationForm());
        return "registration";
    }

    @RequestMapping(value = "/registeruser", method = RequestMethod.POST)
    public String registerUser(@ModelAttribute RegistrationForm form, Model model) {
        if (form.computeValidity(userRepo)) {

            UserEntity userEntity = new UserEntity(form, String.valueOf(random.nextInt(12412953)));
            userRepo.save(userEntity);

            Message message = new MimeMessage(session);
            try {
                message.setFrom(new InternetAddress(emailUser));
                message.setRecipient(Message.RecipientType.TO, new InternetAddress(userEntity.getEmailAddress()));
                message.setSubject("Email Confirmation.");
                String content = "Please click <a href='lovejoy.ashleigh.rocks/confirmemail?username=" +
                        userEntity.getName() +  "&token=" + userEntity.getToken() + ">HERE</a> to confirm your email!";
                MimeBodyPart mimeBodyPart = new MimeBodyPart();
                mimeBodyPart.setContent(content, "text/html; charset=utf-8");

                Multipart multipart = new MimeMultipart();
                multipart.addBodyPart(mimeBodyPart);

                message.setContent(multipart);
                Transport.send(message);

            } catch (Exception e) {
                e.printStackTrace();
                return "redirect:/";
            }

            return "emailconfirmation";
        }

        form.setPassword("");
        form.setPasswordConfirmation("");
        model.addAttribute("user", form);
        model.addAttribute("errors", form.getErrors());
        return "registration";
    }

    @GetMapping("/confirmemail")
    public String confirmEmail(@RequestParam("username") String name, @RequestParam("token") String token) {
        UserEntity user = userRepo.findById(name).get();
        if (user.equals(null)) {
            return "redirect:/";
        }

        if (user.getToken().equals(token)) {
            user.setEmailConfirmed(true);
            userRepo.save(user);
            return "successemail";
        }
        return "failemail";


    }


    /** -- LOGIN -- **/
    @GetMapping("/login")
    public String loginPage(Model model, HttpSession session) {
        if (session.getAttribute("login") != null) {
            return "redirect:/";
        }
        model.addAttribute("user", new LoginForm());
        return "login";
    }

    @RequestMapping(value = "/loginuser", method = RequestMethod.POST)
    public String loginUser(@ModelAttribute LoginForm form, Model model, HttpSession session) {
        UserEntity userEntity = userRepo.findById(form.getUsername()).get();
        if (userEntity != null && userEntity.comparePassword(form.getPassword())) {
            session.setAttribute("login", form.getUsername());
            if (userEntity.isAdmin()) {
                session.setAttribute("admin", true);
            }
            return "redirect:/";
        }

        form.setPassword("");
        model.addAttribute("user", form);
        model.addAttribute("error", "Incorrect username or password!");
        return "login";
    }

    @GetMapping("/logout")
    public RedirectView logoutPage(HttpSession session) {
        session.removeAttribute("login");
        session.removeAttribute("admin");
        return new RedirectView("/");
    }

    // TODO
    @GetMapping("/passwordrecovery")
    public String recoveryPage() {
        return "recovery";
    }

    // TODO
    @GetMapping("/requestevaluation")
    public String requestPage(Model model, HttpSession session) {
        if (session.getAttribute("login") == null) {
            return "redirect:/";
        }
        model.addAttribute("request", new EvaluationRequest());
        return "request";
    }

    @PostMapping("/submitrequest")
    public String submitRequest(@ModelAttribute EvaluationRequest request, HttpSession session, Model model) {
        if (session.getAttribute("login") == null) {
            return "redirect:/";
        }
        if (request.computeValidity()) {
            EvaluationEntity entity = new EvaluationEntity((String) session.getAttribute("login"), request);
            evalRepo.save(entity);
            return "requested";
        }
        model.addAttribute("request", request);
        model.addAttribute("errors", request.getErrors());
        return "request";
    }

    // TODO
    @GetMapping("/evaluationrequests")
    public String evaluationPage(HttpSession session) {
        if (session.getAttribute("login") == null || session.getAttribute("admin") == null) {
            return "redirect:/";
        }
        return "evaluation";
    }

}

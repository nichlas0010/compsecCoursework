package rocks.ashleigh.lovejoy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;
import rocks.ashleigh.lovejoy.datastructures.EvaluationRequest;
import rocks.ashleigh.lovejoy.datastructures.LoginForm;
import rocks.ashleigh.lovejoy.datastructures.RegistrationForm;
import rocks.ashleigh.lovejoy.datastructures.ResetRequest;
import rocks.ashleigh.lovejoy.jpa.EvaluationEntity;
import rocks.ashleigh.lovejoy.jpa.EvaluationRepository;
import rocks.ashleigh.lovejoy.jpa.UserEntity;
import rocks.ashleigh.lovejoy.jpa.UserRepository;

import javax.mail.*;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.*;

@Controller
public class LoveJoyController {
    private UserRepository userRepo;
    private EvaluationRepository evalRepo;
    private Properties prop = new Properties();
    private Random random = new Random();
    private JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

    public LoveJoyController(UserRepository userRepo, EvaluationRepository evalRepo, @Value("${EMAIL_USERNAME}") String emailUser, @Value("${EMAIL_PASSWORD}") String emailPass) {
        this.userRepo = userRepo;
        this.evalRepo = evalRepo;

        mailSender.setUsername(emailUser);
        mailSender.setPassword(emailPass);

        mailSender.setHost("smtp.gmail.com");
        mailSender.setPort(465);

        Properties mailProp = mailSender.getJavaMailProperties();
        mailProp.put("mail.transport.protocol", "smtp");
        mailProp.put("mail.smtp.auth", "true");
        mailProp.put("mail.smtp.starttls.enable", "true");
        mailProp.put("mail.smtp.starttls.required", "true");
        mailProp.put("mail.debug", "true");
        mailProp.put("mail.smtp.ssl.enable", "true");
        mailProp.put("mail.smtp.user", emailUser);

    }

    // DONE
    @GetMapping("/")
    public String mainPage(Model model, HttpSession session) {
        model.addAttribute("loggedin", session.getAttribute("loggedin"));
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

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            try {
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
                helper.setTo(userEntity.getEmailAddress());
                helper.setSubject("Email confirmation");
                helper.setText(
                        "Dear "+userEntity.getName()+", please click <a href='lovejoy.ashleigh.rocks/confirmemail?address=" +
                        userEntity.getEmailAddress() +  "&token=" + userEntity.getToken() + "'>HERE</a> to confirm your email!",
                        true);

                mailSender.send(mimeMessage);
            } catch (MessagingException e) {
                throw new RuntimeException("Error sending email to user", e);
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
    public String confirmEmail(@RequestParam("address") String name, @RequestParam("token") String token) {
        UserEntity user = userRepo.findByEmailAddress(name);
        if (user == null) {
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
        if (session.getAttribute("loggedin") != null) {
            return "redirect:/";
        }
        model.addAttribute("user", new LoginForm());
        return "login";
    }

    @PostMapping("/loginuser")
    public String loginUser(@ModelAttribute LoginForm form, Model model, HttpSession session) {

        UserEntity userEntity = userRepo.findByEmailAddress(form.getEmailAddress());
        if (userEntity != null && userEntity.getLastLogin() != null && LocalDateTime.now().minusSeconds(5).compareTo(userEntity.getLastLogin()) <= 0 ) {
            model.addAttribute("error", "Please wait 5 seconds before attempting to login again!");

        } else if (userEntity != null && userEntity.comparePassword(form.getPassword())) {
            session.setAttribute("login", userEntity.getEmailAddress());
            String pin = String.valueOf(random.nextInt(9999));
            session.setAttribute("pin", pin);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            try {
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
                helper.setTo(userEntity.getEmailAddress());
                helper.setSubject("Login Pin");
                helper.setText(
                        "Dear "+userEntity.getName()+", please enter this pin to login: " + pin,
                        true);

                mailSender.send(mimeMessage);
            } catch (MessagingException e) {
                throw new RuntimeException("Error sending email containing pin to user ", e);
            }

            if (userEntity.isAdmin()) {
                session.setAttribute("admin", true);
            }
            return "redirect:/askforpin";
        } else {

            model.addAttribute("error", "Incorrect email address or password!");
        }
        form.setPassword("");
        model.addAttribute("user", form);
        return "login";
    }

    @GetMapping("/askforpin")
    public String askForPin(Model model) {
        model.addAttribute("pin", "");
        return "pin";
    }

    @PostMapping("/submitpin")
    public String submitPin(@ModelAttribute("pin") String pin, HttpSession session) {
        if(pin.equals(session.getAttribute("pin"))) {
            session.setAttribute("loggedin", true);
            return "redirect:/";
        } else {
            session.removeAttribute("login");
            session.removeAttribute("admin");
            return "redirect:/login";
        }
    }

    @GetMapping("/logout")
    public RedirectView logoutPage(HttpSession session) {
        session.removeAttribute("loggedin");
        session.removeAttribute("admin");
        return new RedirectView("/");
    }

    // TODO
    @GetMapping("/passwordrecovery")
    public String recoveryPage(Model model) {
        model.addAttribute("address", "");
        return "recovery";
    }

    @PostMapping("/secquest")
    public String securityQuestion(@ModelAttribute("address") String address, Model model) {
        UserEntity userEntity = userRepo.findByEmailAddress(address);
        if (userEntity != null) {
            model.addAttribute("address", address);
            model.addAttribute("secQuestion", userEntity.getSecQuestion());
            model.addAttribute("secAnswer", "");
            return "secquest";
        }
        return "redirect:/";
    }

    @PostMapping("/secanswer")
    public String securityAnswer(@ModelAttribute("address") String address, @ModelAttribute("secAnswer") String answer, Model model) {
        UserEntity userEntity = userRepo.findByEmailAddress(address);
        if (userEntity != null) {
            if (userEntity.getSecAnswer().equals(answer)) {
                userEntity.setResetting(true);
                userRepo.save(userEntity);
                MimeMessage mimeMessage = mailSender.createMimeMessage();
                try {
                    MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
                    helper.setTo(userEntity.getEmailAddress());
                    helper.setSubject("Login Pin");
                    helper.setText(
                            "Dear "+userEntity.getName()+", please go <a href='https://lovejoy.ashleigh.rocks/resetpass?token=" +
                                    userEntity.getToken() + "&address="+ userEntity.getEmailAddress()+"'>HERE</a> to reset your password!",
                            true);

                    mailSender.send(mimeMessage);
                } catch (MessagingException e) {
                    throw new RuntimeException("Error sending email containing password reset to user ", e);
                }

                return "emailconf";
            }

            model.addAttribute("address", address);
            model.addAttribute("secAnswer", "");
            return "secquest";
        }

        return "redirect:/";
    }

    @GetMapping("/resetpass")
    public String resetPass(@RequestParam("token") String token, @RequestParam("address") String address, Model model) {
        UserEntity userEntity = userRepo.findByEmailAddress(address);
        if (userEntity != null) {
            if(userEntity.isResetting() && userEntity.getToken().equals(token)) {
                ResetRequest reset = new ResetRequest();
                reset.setAddress(address);
                model.addAttribute("reset", reset);
                return "resetpass";
            }
        }
        return "redirect:/";
    }

    @PostMapping("/resetpass")
    public String resetPassword(@RequestBody ResetRequest reset, Model model) {
        if(reset.computeValidity()) {
            UserEntity userEntity = userRepo.findByEmailAddress(reset.getAddress());
            if (userEntity != null) {
                userEntity.newPassword(reset.getPassword());
                userEntity.setResetting(false);
                userRepo.save(userEntity);
                return "successpass";
            }
        }
        model.addAttribute("errors", reset.getErrors());
        return "resetpass";
    }

    @GetMapping("/requestevaluation")
    public String requestPage(Model model, HttpSession session) {
        if (session.getAttribute("loggedin") == null) {
            return "redirect:/";
        }
        model.addAttribute("request", new EvaluationRequest());
        return "request";
    }

    @PostMapping("/submitrequest")
    public String submitRequest(@ModelAttribute EvaluationRequest request, HttpSession session, Model model) {
        if (session.getAttribute("loggedin") == null) {
            return "redirect:/";
        }
        if (request.computeValidity()) {
            EvaluationEntity entity = new EvaluationEntity(userRepo.findByEmailAddress((String) session.getAttribute("login")), request);
            evalRepo.save(entity);
            return "requested";
        }
        model.addAttribute("request", request);
        model.addAttribute("errors", request.getErrors());
        return "request";
    }

    // TODO
    @GetMapping("/evaluationrequests")
    public String evaluationPage(HttpSession session, Model model) {
        if (session.getAttribute("loggedin") == null || session.getAttribute("admin") == null) {
            return "redirect:/";
        }

        ArrayList<EvaluationEntity> evals = new ArrayList<>(evalRepo.findAll());
        HashMap<EvaluationEntity, UserEntity> map = new HashMap<>();
        for (EvaluationEntity eval : evals) {
            map.put(eval, userRepo.findByEmailAddress(eval.getEmailAddress()));
        }
        model.addAttribute("requests", map);
        return "evaluationlist";
    }

    @GetMapping(value = "/imgs/{img_id}")
    public ResponseEntity<byte[]> getImage(@PathVariable("img_id") String s) {
        Optional<EvaluationEntity> opt = evalRepo.findById(Integer.valueOf(s));
        if (!opt.isPresent()) {
            return new ResponseEntity<>(new HttpHeaders(), HttpStatus.BAD_REQUEST);
        }
        EvaluationEntity e = opt.get();
        return new ResponseEntity<>(e.getImage(), new HttpHeaders(), HttpStatus.OK);
    }

}

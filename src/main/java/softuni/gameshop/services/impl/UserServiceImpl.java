package softuni.gameshop.services.impl;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import softuni.gameshop.domain.dtos.UserLoginDto;
import softuni.gameshop.domain.dtos.UserRegisterDto;
import softuni.gameshop.domain.entities.Game;
import softuni.gameshop.domain.entities.User;
import softuni.gameshop.domain.enums.Role;
import softuni.gameshop.repositories.UserRepository;
import softuni.gameshop.services.UserService;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private ModelMapper mapper;
    private String loggedInUser;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.mapper = new ModelMapper();
        this.loggedInUser = "";
    }

    @Override
    public String registerUser(UserRegisterDto userRegisterDto) {

        StringBuilder sb = new StringBuilder();
        if (!userRegisterDto.getPassword().equals(userRegisterDto.getConfirmPassword())){
            return "Password don't match";
        }

        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        Validator validator = validatorFactory.getValidator();
        Set<ConstraintViolation<UserRegisterDto>> violations = validator.validate(userRegisterDto);

        User inDb = this.userRepository.findByEmail(userRegisterDto.getEmail()).orElse(null);

        if (violations.size() > 0){
            for (ConstraintViolation<UserRegisterDto> violation : violations) {
                sb.append(violation.getMessage()).append(System.lineSeparator());
            }
        } else if(inDb != null) {
            return sb.append("User is already registered").toString();
        }else {
            User user = this.mapper.map(userRegisterDto, User.class);
            if (this.userRepository.count() == 0){
                user.setRole(Role.ADMIN);
            } else {
                user.setRole(Role.USER);
            }
            this.userRepository.saveAndFlush(user);
            sb.append(String.format("%s was registered", user.getFullName()));
        }

        return sb.toString();
    }

    @Override
    public String loginUser(UserLoginDto userLoginDto) {
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        Validator validator = validatorFactory.getValidator();
        Set<ConstraintViolation<UserLoginDto>> violations = validator.validate(userLoginDto);
        StringBuilder sb = new StringBuilder();

        if (violations.size() > 0){
            for (ConstraintViolation<UserLoginDto> violation : violations) {
                sb.append(violation.getMessage()).append(System.lineSeparator());
            }
        } else if (!this.loggedInUser.isEmpty()){
            return sb.append("Session is taken").toString();
        } else {
            User user = this.userRepository.findByEmail(userLoginDto.getEmail()).orElse(null);
            if (user == null) {
                return sb.append("User does not exist").toString();
            } else {
                if (!user.getPassword().equals(userLoginDto.getPassword())) {
                    return sb.append("Incorrect password").toString();
                }

                this.loggedInUser = user.getEmail();
                sb.append(String.format("Successfully logged in %s", user.getFullName()));
            }
        }

        return sb.toString();
    }

    @Override
    public String logoutUser() {
        StringBuilder sb = new StringBuilder();

        if (this.loggedInUser.isEmpty()){
            sb.append("Cannot log out. No user was logged in");
        } else {
            this.userRepository.findByEmail(this.loggedInUser).ifPresent(user ->
                    sb.append(String.format("User %s successfully logged out.", user.getFullName())));
            this.loggedInUser = "";
        }

        return sb.toString();
    }

    @Override
    public String getUserBoughtGames() {
        StringBuilder sb = new StringBuilder();

        User user = this.userRepository.findByEmail(this.loggedInUser).orElse(null);

        if (user == null){
            return "There's no user logged in!";
        }
        Set<Game> games = user.getGames();

        if (games.isEmpty()){
            return "There are not bought games from this user!";
        }

        for (Game game : games) {
            sb.append(game.getTitle()).append(System.lineSeparator());
        }

        return sb.toString();

    }

}

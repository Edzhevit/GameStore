package softuni.gameshop.services.impl;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import softuni.gameshop.domain.dtos.GameAddDto;
import softuni.gameshop.domain.entities.Game;
import softuni.gameshop.domain.entities.User;
import softuni.gameshop.domain.enums.Role;
import softuni.gameshop.repositories.GameRepository;
import softuni.gameshop.repositories.UserRepository;
import softuni.gameshop.services.GameService;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

@Service
public class GameServiceImpl implements GameService {

    private final GameRepository gameRepository;
    private final UserRepository userRepository;
    private ModelMapper modelMapper;
    private String loggedInUser;

    @Autowired
    public GameServiceImpl(GameRepository gameRepository, UserRepository userRepository) {
        this.gameRepository = gameRepository;
        this.userRepository = userRepository;
        this.modelMapper = new ModelMapper();
    }

    @Override
    public String addGame(GameAddDto gameAddDto) {
        StringBuilder sb = new StringBuilder();

        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        Validator validator = validatorFactory.getValidator();
        Set<ConstraintViolation<GameAddDto>> violations = validator.validate(gameAddDto);

        if (violations.size() > 0){
            for (ConstraintViolation<GameAddDto> violation : violations) {
                sb.append(violation.getMessage()).append(System.lineSeparator());
            }

            return sb.toString();
        }

        Game gameEntity = this.modelMapper.map(gameAddDto, Game.class);

        Game game = this.gameRepository.findGameByTitle(gameAddDto.getTitle()).orElse(null);

        if (game == null){
            User user = this.userRepository.findByEmail(loggedInUser).orElse(null);
            if (user == null){
                return sb.append("No user is logged in").toString();
            } else if (!user.getRole().equals(Role.ADMIN)){
                return sb.append(String.format("%s is not admin", user.getFullName())).toString();
            } else {
                this.gameRepository.saveAndFlush(gameEntity);
                Set<Game> games = user.getGames();
                games.add(gameEntity);
                user.setGames(games);
                this.userRepository.saveAndFlush(user);
                sb.append(String.format("Added %s", gameEntity.getTitle()));
            }
        } else {
            return sb.append("Game already exists").toString();
        }


        return sb.toString();
    }

    @Override
    public String editGame(int id, String parameter, String value) {
        StringBuilder sb = new StringBuilder();

        User user = this.userRepository.findByEmail(loggedInUser).orElse(null);

        if (user == null){
            return sb.append("No user is logged in").toString();
        }

        if (!user.getRole().equals(Role.ADMIN)){
            return sb.append(String.format("%s is not admin", user.getFullName())).toString();
        }

        Game game = this.gameRepository.findById(id);
        if (game == null){
            return sb.append("No such game").toString();
        }

        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        Validator validator = validatorFactory.getValidator();
        Set<ConstraintViolation<Game>> violations = validator.validate(game);

        if (violations.size() > 0){
            for (ConstraintViolation<Game> violation : violations) {
                sb.append(violation.getMessage()).append(System.lineSeparator());
            }

            return sb.toString();
        }

        switch (parameter){
            case "price":
                game.setPrice(new BigDecimal(value));
                break;
            case "size":
                game.setSize(Double.parseDouble(value));
                break;
            case "description":
                game.setDescription(value);
                break;
            case "thumbnailURL":
                game.setImageThumbnail(value);
                break;
            case "trailer":
                game.setTrailer(value);
                break;
            case "title":
                game.setTitle(value);
                break;
            case "releaseDate":
                game.setReleaseDate(LocalDate.parse(value, DateTimeFormatter.ofPattern("dd-MM-yyyy")));
                break;
        }

        this.gameRepository.save(game);

        sb.append(String.format("Edited %s", game.getTitle()));
        return sb.toString();
    }

    @Override
    public String deleteGame(int id) {
        StringBuilder sb = new StringBuilder();

        User user = this.userRepository.findByEmail(loggedInUser).orElse(null);

        if (user == null){
            return sb.append("No user is logged in").toString();
        }

        if (!user.getRole().equals(Role.ADMIN)){
            return sb.append(String.format("%s is not admin", user.getFullName())).toString();
        }
        Game game = this.gameRepository.findById(id);

        if (game == null){
            return sb.append("No such game").toString();
        }

        this.gameRepository.delete(game);

        sb.append(String.format("%s deleted", game.getTitle()));
        return sb.toString();
    }

    @Override
    public String allGames() {
        StringBuilder sb = new StringBuilder();
        List<Game> games = this.gameRepository.findGamesBy();
        for (Game game : games) {
            sb.append(String.format("%s %s", game.getTitle(), game.getPrice())).append(System.lineSeparator());
        }
        return sb.toString();
    }

    @Override
    public String gameByTitle(String title) {
        StringBuilder sb = new StringBuilder();

        Game game = this.gameRepository.findGameByTitle(title).orElse(null);

        if (game == null) {
            return "There's no game with this title in the database!";
        }
        sb.append(String.format("Title: %s\nPrice: %s\nDescription: %s\nRelease date: %s",
                game.getTitle(), game.getPrice(), game.getDescription(), game.getReleaseDate()));

        return sb.toString();
    }

    @Override
    public void setLoggedInUser(String email) {
        this.loggedInUser = email;
    }

    @Override
    public void logoutUser() {
        this.loggedInUser = "";
    }
}

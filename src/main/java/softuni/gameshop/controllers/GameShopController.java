package softuni.gameshop.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Controller;
import softuni.gameshop.domain.dtos.GameAddDto;
import softuni.gameshop.domain.dtos.UserLoginDto;
import softuni.gameshop.domain.dtos.UserRegisterDto;
import softuni.gameshop.services.GameService;
import softuni.gameshop.services.UserService;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

// First type in your password in application.properties to connect to database

@Controller
public class GameShopController implements CommandLineRunner {

    private BufferedReader reader;
    private final UserService userService;
    private final GameService gameService;

    @Autowired
    public GameShopController(UserService userService, GameService gameService) {
        this.gameService = gameService;
        this.reader = new BufferedReader(new InputStreamReader(System.in));
        this.userService = userService;
    }

    @Override
    public void run(String... args) throws Exception {

        while (true){
            String[] params = this.reader.readLine().split("\\|");
            switch (params[0]){
                case "RegisterUser":
                    UserRegisterDto userRegisterDto = new UserRegisterDto(params[1], params[2], params[3], params[4]);
                    System.out.println(this.userService.registerUser(userRegisterDto));
                    break;
                case "LoginUser":
                    UserLoginDto userLoginDto = new UserLoginDto(params[1], params[2]);
                    System.out.println(this.userService.loginUser(userLoginDto));
                    this.gameService.setLoggedInUser(userLoginDto.getEmail());
                    break;
                case "LogoutUser":
                    System.out.println(this.userService.logoutUser());
                    this.gameService.logoutUser();
                    break;
                case "AddGame":
                    GameAddDto gameAddDto = new GameAddDto(params[1], params[4], params[5],
                            Double.parseDouble(params[3]), new BigDecimal(params[2]), params[6],
                            LocalDate.parse(params[7], DateTimeFormatter.ofPattern("dd-MM-yyyy")));
                    System.out.println(this.gameService.addGame(gameAddDto));
                    break;
                case "EditGame":
                    int idToEdit = Integer.parseInt(params[1]);
                    for (int i = 2; i <params.length; i++) {
                        String[] info = params[i].split("=");
                        System.out.println(this.gameService.editGame(idToEdit, info[0], info[1]));
                    }
                    break;
                case "DeleteGame":
                    int idToDelete = Integer.parseInt(params[1]);
                    System.out.println(this.gameService.deleteGame(idToDelete));
                    break;
                case "AllGames":
                    System.out.println(this.gameService.allGames());
                    break;
                case "DetailGame":
                    String title = params[1];
                    System.out.println(this.gameService.gameByTitle(title));
                    break;
                case "OwnedGames":
                    System.out.println(this.userService.getUserBoughtGames());
                    break;
            }
        }
    }
}

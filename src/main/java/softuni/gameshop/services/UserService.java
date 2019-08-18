package softuni.gameshop.services;

import softuni.gameshop.domain.dtos.UserLoginDto;
import softuni.gameshop.domain.dtos.UserRegisterDto;

public interface UserService {

    String registerUser(UserRegisterDto userRegisterDto);

    String loginUser(UserLoginDto userLoginDto);

    String logoutUser();

    String getUserBoughtGames();
}

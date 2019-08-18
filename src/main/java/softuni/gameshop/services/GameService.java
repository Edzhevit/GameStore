package softuni.gameshop.services;

import softuni.gameshop.domain.dtos.GameAddDto;

public interface GameService {
    String addGame(GameAddDto gameAddDto);

    String editGame(int id, String parameter, String value);

    String deleteGame(int id);

    String allGames();

    String gameByTitle(String title);

    void setLoggedInUser(String email);

    void logoutUser();
}

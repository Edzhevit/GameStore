package softuni.gameshop.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import softuni.gameshop.domain.entities.Game;
import java.util.List;
import java.util.Optional;

@Repository
public interface GameRepository extends JpaRepository<Game, Integer> {
    Game findById(int id);

    Game deleteById(int id);

    List<Game> findGamesBy();

    Optional<Game> findGameByTitle(String title);
}

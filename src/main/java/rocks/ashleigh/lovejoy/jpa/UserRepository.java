package rocks.ashleigh.lovejoy.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, String> {

    public UserEntity findByEmailAddress(String s);
}

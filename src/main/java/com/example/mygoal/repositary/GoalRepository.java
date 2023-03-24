package com.example.mygoal.repositary;

import com.example.mygoal.model.Goal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface GoalRepository extends JpaRepository<Goal,Integer> {
    @Modifying
    @Transactional
    void deleteBySportType(String sportType);

//    @Query("SELECT g.value FROM Goal g WHERE g.id=:id")
//    String findById(@Param("id") int id);
@Query("SELECT g.value FROM Goal g WHERE g.sportType=:sportType")
String findBySportType(@Param("sportType") String sportType);

    @Modifying
    @Transactional
    @Query(value = "INSERT IGNORE INTO Goal ( sport_type, goal_type, timeframe, value) VALUES (:sportType, :goalType, :timeframe, :value)", nativeQuery = true)
    void saveUnique(@Param("sportType") String sportType, @Param("goalType") String goalType, @Param("timeframe") String timeframe, @Param("value") String value);
}
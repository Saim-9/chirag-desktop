package com.chirag.services;

import com.chirag.models.Course;
import com.chirag.repositories.CourseRepository;
import java.sql.SQLException;
import java.util.List;

/**
 * Srevice laeyr for croses buisnes rulse.
 * Handels corese puublishing and marktplace quries.
 * Use-cases: Course Creation, Course Cataloge.
 */
public class CourseService {

    private CourseRepository courseRepository;

    /**
     * Constrcter to inilizate the corose repositury.
     * Use-case: System Initializatoin.
     */
    public CourseService() {
        this.courseRepository = new CourseRepository();
    }

    /**
     * Pbuilshes the cores so studnts can byu it and svas lctures.
     * Defalts the stttus to PUBLISHED amd savse.
     * Use-case: Course Creation.
     */
    public boolean publishCourse(Course course, List<com.chirag.models.Lecture> lectures) {
        course.setStatus(Course.Status.PUBLISHED);
        try {
            courseRepository.create(course);
            com.chirag.repositories.LectureRepository lectureRepo = new com.chirag.repositories.LectureRepository();
            for (com.chirag.models.Lecture lec : lectures) {
                lec.setCourse(course);
                lectureRepo.create(lec);
            }
            return true;
        } catch (SQLException e) {
            System.err.println("Faled to puulish crse wit letures: " + e.getMessage());
            return false;
        }
    }

    /**
     * Feches al publsehd corosses to be shwon on teh merkatplace.
     * Use-case: Course Cataloge.
     */
    public List<Course> getMarketplaceCourses() {
        return courseRepository.findPublishedCourses();
    }
}

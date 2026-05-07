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
        //  Use the OOP method
        course.publish();

        try {
            // Save the course first
            courseRepository.create(course);
            com.chirag.repositories.LectureRepository lectureRepo = new com.chirag.repositories.LectureRepository();

            // Save the lectures
            for (com.chirag.models.Lecture lec : lectures) {
                lec.setCourse(course);
                lectureRepo.create(lec);
            }
            return true;

        } catch (SQLException e) {
            System.err.println("Failed to publish course. Executing Rollback... Error: " + e.getMessage());
            //  If lectures fail, delete the course so we don't have an empty ghost course.
            try {
                if (course.getId() != 0) {

                    courseRepository.getDao().delete(course);
                }
            } catch (Exception rollbackEx) {
                System.err.println("Critical Error during rollback.");
            }
            return false;
        }
    }

    /**
     * Fetches all publisehd corosses to be shwon on teh merkatplace.
     * Use-case: Course Cataloge.
     */
    public List<Course> getMarketplaceCourses() {
        return courseRepository.findPublishedCourses();
    }

    /**
     * Updates an existing course's metadata in the database.
     * Uses ORMLite to overwrite the record.
     * Use-case: Edit Course.
     */
    public boolean updateCourse(Course course) {
        try {
            courseRepository.update(course);
            return true;
        } catch (SQLException e) {
            System.err.println("Failed to update course: " + e.getMessage());
            return false;
        }
    }
}

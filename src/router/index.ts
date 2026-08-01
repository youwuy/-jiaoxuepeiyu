import { createRouter, createWebHistory } from 'vue-router';
import AdminLogin from '../views/auth/AdminLogin.vue';
import AdminCourseForm from '../views/admin/AdminCourseForm.vue';
import AdminCourseTheoryReview from '../views/admin/AdminCourseTheoryReview.vue';
import AdminCourseReviews from '../views/admin/AdminCourseReviews.vue';
import AdminCourseStatistics from '../views/admin/AdminCourseStatistics.vue';
import AdminCourses from '../views/admin/AdminCourses.vue';
import AdminDeviceEfficiency from '../views/admin/AdminDeviceEfficiency.vue';
import AdminOrganization from '../views/admin/AdminOrganization.vue';
import AdminPermissions from '../views/admin/AdminPermissions.vue';
import AdminProfile from '../views/admin/AdminProfile.vue';
import AdminRoles from '../views/admin/AdminRoles.vue';
import AdminSettings from '../views/admin/AdminSettings.vue';
import AdminPublicApplications from '../views/admin/AdminPublicApplications.vue';
import AdminPublicResources from '../views/admin/AdminPublicResources.vue';
import AdminResources from '../views/admin/AdminResources.vue';
import AdminSemesterScore from '../views/admin/AdminSemesterScore.vue';
import AdminTheoryPapers from '../views/admin/AdminTheoryPapers.vue';
import AdminTheoryQuestions from '../views/admin/AdminTheoryQuestions.vue';
import AdminTrainingArchive from '../views/admin/AdminTrainingArchive.vue';
import AdminTrainingCourses from '../views/admin/AdminTrainingCourses.vue';
import AdminUsers from '../views/admin/AdminUsers.vue';
import StudentLogin from '../views/auth/StudentLogin.vue';
import StudentCourseLearn from '../views/student/StudentCourseLearn.vue';
import StudentCourses from '../views/student/StudentCourses.vue';
import StudentProfile from '../views/student/StudentProfile.vue';
import StudentResources from '../views/student/StudentResources.vue';
import StudentTraining from '../views/student/StudentTraining.vue';

export const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      redirect: '/student/login'
    },
    {
      path: '/student/login',
      name: 'student-login',
      component: StudentLogin
    },
    {
      path: '/student/courses',
      name: 'student-courses',
      component: StudentCourses
    },
    {
      path: '/student/courses/:id/learn',
      name: 'student-course-learn',
      component: StudentCourseLearn
    },
    {
      path: '/student/training',
      name: 'student-training',
      component: StudentTraining
    },
    {
      path: '/student/resources',
      name: 'student-resources',
      component: StudentResources
    },
    {
      path: '/student/profile',
      name: 'student-profile',
      component: StudentProfile
    },
    {
      path: '/admin/login',
      name: 'admin-login',
      component: AdminLogin
    },
    {
      path: '/admin',
      redirect: '/admin/organization'
    },
    {
      path: '/admin/organization',
      name: 'admin-organization',
      component: AdminOrganization
    },
    {
      path: '/admin/users',
      name: 'admin-users',
      component: AdminUsers
    },
    {
      path: '/admin/permissions',
      name: 'admin-permissions',
      component: AdminPermissions
    },
    {
      path: '/admin/roles',
      name: 'admin-roles',
      component: AdminRoles
    },
    {
      path: '/admin/settings',
      name: 'admin-settings',
      component: AdminSettings
    },
    {
      path: '/admin/profile',
      name: 'admin-profile',
      component: AdminProfile
    },
    {
      path: '/admin/personal-resource',
      name: 'admin-personal-resource',
      component: AdminResources
    },
    {
      path: '/admin/public-application',
      name: 'admin-public-application',
      component: AdminPublicApplications
    },
    {
      path: '/admin/public-resource',
      name: 'admin-public-resource',
      component: AdminPublicResources
    },
    {
      path: '/admin/theory-question',
      name: 'admin-theory-question',
      component: AdminTheoryQuestions
    },
    {
      path: '/admin/theory-paper',
      name: 'admin-theory-paper',
      component: AdminTheoryPapers
    },
    {
      path: '/admin/courses',
      name: 'admin-courses',
      component: AdminCourses
    },
    {
      path: '/admin/courses/new',
      name: 'admin-course-new',
      component: AdminCourseForm
    },
    {
      path: '/admin/courses/:id/statistics',
      name: 'admin-course-statistics',
      component: AdminCourseStatistics
    },
    {
      path: '/admin/courses/:id/reviews',
      name: 'admin-course-reviews',
      component: AdminCourseReviews
    },
    {
      path: '/admin/courses/:id/reviews/:reviewId/theory',
      name: 'admin-course-theory-review',
      component: AdminCourseTheoryReview
    },
    {
      path: '/admin/training',
      name: 'admin-training',
      component: AdminTrainingCourses
    },
    {
      path: '/admin/semester-score',
      name: 'admin-semester-score',
      component: AdminSemesterScore
    },
    {
      path: '/admin/training-archive',
      name: 'admin-training-archive',
      component: AdminTrainingArchive
    },
    {
      path: '/admin/device-efficiency',
      name: 'admin-device-efficiency',
      component: AdminDeviceEfficiency
    }
  ]
});

import {User} from "./user";

export class Student extends User {
  courseEnrolledIds: string[] = [];  //id's of the course enrolled
}

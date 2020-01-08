import {User} from "./user";

export class Instructor extends User {
  courseInstructed: string[] = [];  //id's of the course instructed
}

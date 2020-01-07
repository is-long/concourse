export class Course{
  id: string;  //course id
  name: string; //course display name
  description: string;
  creatorInstructorId: string;

  instructorIds: string[];
  studentIds: string[];
  questionRootIds: string[];
}

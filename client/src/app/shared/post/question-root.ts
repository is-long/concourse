import {Post} from "./post";

export class QuestionRoot extends Post {
  viewCount: number;
  viewerIds: [];

  questionRootAnswerIds: string[];
  followupQuestionIds: string[];
}

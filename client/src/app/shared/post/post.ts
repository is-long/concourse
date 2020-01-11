export class Post {
  id: string;
  courseId: string;

  postDate: number;
  content: string;
  likeCount: number = 0;

  authorUserId: string;
  authorName: string;
  authorType: string;

  likesUserIDMap: Map<string, number> = new Map();
}

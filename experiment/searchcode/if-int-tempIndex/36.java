package bulletin_publish.com;

public class BulletinFile {
		
    public static String CreateHeading(int headingNum, String file)
    {
        String post = "";
        String heading = "";
        int index = 0;
        int counter = 0;
        int lastIndex = 0;

        //check for unwanted calls
        if (file == "")
        {
            return " ";
        }

        //go through the file String which contains the whole file and find the post
        do
        {
            lastIndex = index;
            index = file.indexOf(";@", lastIndex + 1);
            counter++;
        } while (headingNum != counter && lastIndex != -1 && index != -1);

        if (lastIndex == -1 || index == -1)  //last line of file is reached
        {
            return " ";
        }

        if (headingNum != 1)
        {
            post = file.substring(lastIndex + 4, index - 4);
        }
        else
        {
            post = file.substring(lastIndex, index);
        }

        index = 0;  //reset index

        //find each field and update the passed variables
        lastIndex = index;
        
        
        index = post.indexOf(":@", lastIndex + 1);
        heading += post.substring(lastIndex, index) + ": " + GetTitle(headingNum, file) + " Creator:";

        lastIndex = index;
        index = post.indexOf(":@", lastIndex + 1);

        lastIndex = index;
        index = post.indexOf(":@", lastIndex + 1);
        heading += post.substring(lastIndex, index) + " Date&time:";
        
//        lastIndex = index;
//        index = post.indexOf(":@", lastIndex + 1);
        //content = post.substring(lastIndex, index - lastIndex);

        lastIndex = index;
        index = post.indexOf(":@", lastIndex + 1);

        lastIndex = index;
        index = post.indexOf(":@", lastIndex + 1);
        heading += post.substring(lastIndex, index);

        heading = heading.replace("\n", "");  //remove white sapce

        return heading;
    }
    
    public static int _GetComment(int headingNum, String file, int commentNum)
    {
        String comments = "";
        String[] commentArray = new String[64];
        String foundComment = "COMMENT_NOT_FOUND";
        int returnNumber = -1;
        
        comments = GetComments(headingNum, file).concat(":@;@"); 

        if(comments.startsWith(":@;@"))
        {
        	return -1;
        }
       
        //go through the comments and extract each comment and put in array
        for (int x = 0; x < 64; x++)
        {
        	if(comments.lastIndexOf(":@") == comments.indexOf(":@") && comments.length() > 4)
        	{
        		commentArray[x] = comments.replace(":@", "");

        		if (commentNum == Integer.parseInt(comments.substring(2, comments.indexOf(":", 2))))  //each comment will have a unique number 1-64
                {
                    foundComment = comments.replace(":@", "");
                    break;
                }
        	}
        	else
        	{
        		commentArray[x] = comments.substring(2, comments.indexOf(":@", 2));
                comments = comments.substring(comments.indexOf(":@" , 1), comments.length() - 1);
        	}
             
            if (commentNum == Integer.parseInt(commentArray[x].substring(0, commentArray[x].indexOf(":"))))  //each comment will have a unique number 1-64
            {
                foundComment = commentArray[x].toString();//.replace("-@-", "\r");
                break;
            }
            
            returnNumber = x;
        }

        return returnNumber;
    }
    
    public static String GetHeading(int headingNum, String file)
    {
        String post = "";
        String heading = "";
        int index = 0;
        int counter = 0;
        int lastIndex = 0;

        //check for unwanted calls
        if (file == "")
        {
            return "";
        }

        //go through the file String which contains the whole file and find the post
        do
        {
            lastIndex = index;
            
            counter = Integer.parseInt(file.substring(lastIndex, file.indexOf(":", lastIndex)));
            
            index = file.indexOf(";@", lastIndex + 1) + 4;

        } while (headingNum != counter && lastIndex != -1 && index != -1);

        if (lastIndex == -1 || index == -1)  //when last line of file is reached
        {
            return "";
        }

        if (headingNum == 1)
        {
            post = file.substring(lastIndex + 4, index - 4);
        }
        else
        {
            post = file.substring(lastIndex + 3, index - 3);
        }

        index = 0;  //reset index

        //find the heading in the found post and build the text 
        for (int x = 0; x < 3; x++)  // 
        {
            lastIndex = index;
            index = post.indexOf(":@", lastIndex + 1);
            heading += post.substring(lastIndex, index);
        }
              
        return heading;
    }

    public static String GetPost(int headingNum, String file)
    {
        String post = "";
        int index = 0;
        int counter = 0;
        int lastIndex = 0;

        //check for unwanted calls
        if (file == "")
        {
            return "";
        }

        //go through the file String which contains the whole file and find the post
        do
        {
            lastIndex = index;
            index = file.indexOf(";@", lastIndex + 1);
            counter++;
        } while (headingNum != counter && lastIndex != -1 && index != -1);

        if (lastIndex == -1 || index == -1)  //when last line of file is reached
        {
            return "";
        }

        post = file.substring(lastIndex + 4, index - 1);

        return post.replace("\n", "");
    }


    public static String GetContent(int headingNum, String file)
    {
        String post = "";
        String temp = "";
        String temp2 = "";
        String content = "";
        String comment = "";
//           String commentHeading = "";
        int index = 0;
        int counter = 0;
        int lastIndex = 0;
//        int tempIndex = 0;

        //go through the file String which contains the whole file and find the post
        do
        {
            lastIndex = index;
            index = file.indexOf(";@", lastIndex + 1);
            counter++;
        } while (headingNum != counter && lastIndex != -1 && index != -1);

        if (lastIndex == -1 || index == -1)  //last line of file is reached
        {
            return "";
        }

        if (headingNum != 1)  //first line will not have newline chars in front of the line unlike all other lines
        {
            post = file.substring(lastIndex + 4, index - 4);
        }
        else
        {
            post = file.substring(lastIndex, index);
        }

        index = 0;  //reset index

        //find the content in the found post
        for (int x = 0; x < 4; x++)  // 
        {
            lastIndex = index;
            index = post.indexOf(":@", lastIndex + 1);
        }

        temp = post.substring(lastIndex, index);
/*
        //wrap lines
        while (temp.Length - 41 > tempIndex)
        {
            tempIndex += 41;
            temp = temp.Insert(tempIndex, "\r\n");
            tempIndex += 4;
        }
*/
        content = temp;

        //skip date field
        lastIndex = index;
        index = post.indexOf(":@", lastIndex + 1);

        //add delimiter
        post += ";@";

        while (post.indexOf(":@;@") != index)
        {
//               tempIndex = 0;
            lastIndex = index;
            index = post.indexOf(":@", lastIndex + 1);

            temp2 = post.substring(lastIndex, index);
/*
            //wrap lines
            while (temp2.Length - 41 > tempIndex)
            {
                tempIndex += 41;
                temp2 = temp2.Insert(tempIndex, "\r\n");
                tempIndex += 4;
            }
*/
            comment += temp2 + "\r\n\r\n";
            
        }


        return content.replace("-@-", "\r") + "\r\n------------------------------------\r\n\r\n" + comment.replace("-@-", "\r");
    }

    public static String GetComments(int headingNum, String file)
    {
        String post = "";
        String temp2 = "";
        String comment = "";
        int index = 0;
        int counter = 0;
        int lastIndex = 0;

        //go through the file String which contains the whole file and find the post
        do
        {
            lastIndex = index;
            
            counter = Integer.parseInt(file.substring(lastIndex, file.indexOf(":", lastIndex)));
            
            index = file.indexOf(";@", lastIndex + 1) + 4;
            
        } while (headingNum != counter && lastIndex != -1 && index != -1);

        if (lastIndex == -1 || index == -1)  //last line of file is reached
        {
            return "";
        }

        if (headingNum != 1)  //first line will not have newline chars in front of the line unlike all other lines
        {
            post = file.substring(lastIndex, index - 4);
        }
        else
        {
            post = file.substring(lastIndex, index);
        }

        index = 0;  //reset index

        //find the content in the found post
        for (int x = 0; x < 4; x++)  // 
        {
            lastIndex = index;
            index = post.indexOf(":@", lastIndex + 1);
        }

        //skip date field
        lastIndex = index;
        index = post.indexOf(":@", lastIndex + 1);

        //add delimiter
        post += ":@;@";

        while (post.indexOf(":@;@") - index > 4)
        {
            lastIndex = index;
            index = post.indexOf(":@", lastIndex + 1);

            if(index == -1)
            {
            	break;
            }
            else if(lastIndex == -1)  //single comment only
            {
            	comment = "";
            	break;
            }
            	
            
            temp2 = post.substring(lastIndex, index);

            comment += temp2;
        }

        return comment;
    }

    public static String GetComment(int headingNum, String file, int commentNum)
    {
        String comments = "";
        String[] commentArray = new String[64];
        String foundComment = "COMMENT_NOT_FOUND";
        
        comments = GetComments(headingNum, file).concat(":@;@"); 

        if(comments.startsWith(":@;@"))
        {
        	return "COMMENT_NOT_FOUND";
        }
       
        //go through the comments and extract each comment and put in array
        for (int x = 0; x < 64; x++)
        {
        	if(comments.lastIndexOf(":@") == comments.indexOf(":@") && comments.length() > 4)
        	{
        		commentArray[x] = comments.replace(":@", "");

        		if (commentNum == Integer.parseInt(comments.substring(2, comments.indexOf(":", 2))))  //each comment will have a unique number 1-64
                {
                    foundComment = comments.replace(":@", "");
                    break;
                }
        	}
        	else
        	{
        		commentArray[x] = comments.substring(2, comments.indexOf(":@", 2));
                comments = comments.substring(comments.indexOf(":@" , 1), comments.length() - 1);
        	}
             
            if (commentNum == Integer.parseInt(commentArray[x].substring(0, commentArray[x].indexOf(":"))))  //each comment will have a unique number 1-64
            {
                foundComment = commentArray[x].toString();//.replace("-@-", "\r");
                break;
            }
        }

        return foundComment;
    }

    public static String GetName(int headingNum, String file)
    {
        String post = "";
        int index = 0;
        int counter = 0;
        int lastIndex = 0;

        //go through the file String which contains the whole file and find the post
        do
        {
            lastIndex = index;
            index = file.indexOf(";@", lastIndex + 1);
            counter++;
        } while (headingNum != counter && lastIndex != -1 && index != -1);

        if (lastIndex == -1 || index == -1)  //last line of file is reached
        {
            return " ";
        }

        if (headingNum != 1)
        {
            post = file.substring(lastIndex + 4, index - 4);
        }
        else
        {
            post = file.substring(lastIndex, index);
        }


        index = 0;  //reset index

        //find each field and update the passed variables
        lastIndex = index;
        index = post.indexOf(":@", lastIndex + 1);

        lastIndex = index;
        index = post.indexOf(":@", lastIndex + 1);
//        title = post.substring(lastIndex, index - lastIndex);

        lastIndex = index;
        index = post.indexOf(":@", lastIndex + 1);

        return (post.substring(lastIndex, index));

//        lastIndex = index;
//        index = post.indexOf(":@", lastIndex + 1);
//        content = post.substring(lastIndex, index - lastIndex);

//        lastIndex = index;
//        index = post.indexOf(":@", lastIndex + 1);
//        time = post.substring(lastIndex, index - lastIndex);
    }

    public static String GetTitle(int headingNum, String file)
    {
        String post = "";
        int index = 0;
        int counter = 0;
        int lastIndex = 0;

        //go through the file String which contains the whole file and find the post
        do
        {
            lastIndex = index;
            index = file.indexOf(";@", lastIndex + 1);
            counter++;
        } while (headingNum != counter && lastIndex != -1 && index != -1);

        if (lastIndex == -1 || index == -1)  //last line of file is reached
        {
            return " ";
        }

        if (headingNum != 1)
        {
            post = file.substring(lastIndex + 4, index - 4);
        }
        else
        {
            post = file.substring(lastIndex, index);
        }

        index = 0;  //reset index

        //find each field and update the passed variables
        lastIndex = index;
        index = post.indexOf(":@", lastIndex + 1);

        lastIndex = index;
        index = post.indexOf(":@", lastIndex + 1);

        return (post.substring(lastIndex, index));

        //            lastIndex = index;
        //            index = post.indexOf(":@", lastIndex + 1);
        //            return (post.substring(lastIndex, index - lastIndex));

        //            lastIndex = index;
        //            index = post.indexOf(":@", lastIndex + 1);
        //            content = post.substring(lastIndex, index - lastIndex);

        //            lastIndex = index;
        //            index = post.indexOf(":@", lastIndex + 1);
        //            time = post.substring(lastIndex, index - lastIndex);

    }

    public static String GetTime(int headingNum, String file)
    {
        String post = "";
        int index = 0;
        int counter = 0;
        int lastIndex = 0;

        //go through the file String which contains the whole file and find the post
        do
        {
            lastIndex = index;
            index = file.indexOf(";@", lastIndex + 1);
            counter++;
        } while (headingNum != counter && lastIndex != -1 && index != -1);

        if (lastIndex == -1 || index == -1)  //last line of file is reached
        {
            return " ";
        }

        if (headingNum != 1)
        {
            post = file.substring(lastIndex + 4, index - 4);
        }
        else
        {
            post = file.substring(lastIndex, index);
        }


        index = 0;  //reset index

        //find each field and update the passed variables
        lastIndex = index;
        index = post.indexOf(":@", lastIndex + 1);
        
        lastIndex = index;
        index = post.indexOf(":@", lastIndex + 1);
//        title = post.substring(lastIndex, index - lastIndex);

        lastIndex = index;
        index = post.indexOf(":@", lastIndex + 1);
//        return (post.substring(lastIndex, index - lastIndex));

        lastIndex = index;
        index = post.indexOf(":@", lastIndex + 1);
//        content = post.substring(lastIndex, index - lastIndex);

        lastIndex = index;
        index = post.indexOf(":@", lastIndex + 1);

        return (post.substring(lastIndex, index - lastIndex));
    }
/*
    /// <summary>
    /// Adds a new post to the bulletin file on the server by uploading the
    /// post String to the web handler.  After completing the upload, redownload
    /// the file and update the screen with the added post.
    /// </summary>
    /// <param name="title">Title of the new post</param>
    /// <param name="name">Name of the new post</param>
    /// <param name="content">Content of the new post</param>
    /// <param name="bulletin">The main class binding to the screen</param>
    /// <param name="file">The always updated file String which contains the whole bulletin file</param>
    /// <returns>true</returns>
    public static bool AddPost(String title, String name, String content, Bulletin bulletin, String file)
    {
        UriBuilder ub = new UriBuilder("http://------.com/faq/Bulletin/BulletinWriteHandler.ashx");
        WebClient c = new WebClient();

        c.UploadStringCompleted += (sender, e) =>
        {
            //re-initialize main view
            UriBuilder ub2 = new UriBuilder("http://------.com/faq/Bulletin/BulletinHandler.ashx");
            WebClient c2 = new WebClient();

            c2.UploadStringCompleted += (sender2, e2) =>
            {
                file = String.Copy(e2.Result);
                bulletin.Heading1 = BulletinFile.CreateHeading(1, file);
                bulletin.Heading2 = BulletinFile.CreateHeading(2, file);
                bulletin.Heading3 = BulletinFile.CreateHeading(3, file);
                bulletin.Heading4 = BulletinFile.CreateHeading(4, file);
                bulletin.Heading5 = BulletinFile.CreateHeading(5, file);
                bulletin.Heading6 = BulletinFile.CreateHeading(6, file);
                bulletin.Heading7 = BulletinFile.CreateHeading(7, file);
                bulletin.Heading8 = BulletinFile.CreateHeading(8, file);
                bulletin.Heading9 = BulletinFile.CreateHeading(9, file);
                bulletin.Heading10 = BulletinFile.CreateHeading(10, file);
                bulletin.Heading11 = BulletinFile.CreateHeading(11, file);
                bulletin.Heading12 = BulletinFile.CreateHeading(12, file);
                bulletin.Heading13 = BulletinFile.CreateHeading(13, file);
                bulletin.Heading14 = BulletinFile.CreateHeading(14, file);
                bulletin.Heading15 = BulletinFile.CreateHeading(15, file);
                bulletin.Heading16 = BulletinFile.CreateHeading(16, file);
                bulletin.Heading17 = BulletinFile.CreateHeading(17, file);
            };
            c2.UploadStringAsync(ub2.Uri, "");
        };
        c.UploadStringAsync(ub.Uri, title + ":@" + name + ":@" + content + ":@" + DateTime.Now + ":@" + ";@");

        return true;
    }
*/

    public static boolean IsCommentHack(String time, String content, String file)
    {
        int index = 0;
        int index2 = 0;
        String commentInQuestion = "";

        //Check if there is an another comment with same timestamp.  Check all comments with same timestamp.
        while (file.contains(time))
        {
            index = file.indexOf(time);  //locate the post in question
            index = file.indexOf(" ---- ", index);
            index2 = file.indexOf(":@", index);
            commentInQuestion = file.substring(index + 6, index2 - 6);

            //See if content (first ten characters or less) is also the same.  If same, it is an almost a complete duplicate.
            if (commentInQuestion.contains(content))
            {
                return true;  //is a hack
            }

            file.replace(file.substring(index, index + 10),"");  //remove the timestamp so it will not find it again
        }

        return false;
    }

    public static boolean IsPostHack(String time, String content, String file)
    {
        int index = 0;
        int index2 = 0;
        String commentInQuestion = "";

        //Check if there is an another comment with same timestamp.  Check all comments with same timestamp.
        while (file.contains(time))
        {
            index = file.indexOf(":@", index);  //skip name, title fields
            index = file.indexOf(":@", index + 1);
            index = file.indexOf(":@", index + 1);
            index2 = file.indexOf(":@", index + 1);
            commentInQuestion = file.substring(index + 2, index2 - 2);

            //See if content (first ten characters or less) is also the same.  If same, it is an almost a complete duplicate.
            if (commentInQuestion.contains(content))
            {
                return true;  //is a hack
            }

            file.replace(file.substring(index, index + 10),"");  //remove the timestamp so it will not find it again
        }

        return false;
    }
}



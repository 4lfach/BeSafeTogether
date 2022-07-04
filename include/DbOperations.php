<?php
    class DbOperations{

        private $con;

        function __construct(){
            require_once dirname(__FILE__) . '/DbConnect.php';

            $db = new DbConnect();
            $this->con = $db->connect();
        }

        public function createUser($username, $email, $password){

            if($this->isEmailExist($email) == false){

                $stmt = $this->con->prepare("INSERT INTO users (username, password, email) VALUES (?, ?, ?)");
            
                $stmt->bind_param("sss", $username, $password, $email);
    
                if($stmt->execute()){
                    return USER_CREATED;
                } else{
                    return USER_FAILURE;
                }
            } else{
                return USER_EXISTS;
            }    
        }

        public function userLogin($email, $password){
            if($this->isEmailExist($email) == true){
                $hashed_password = $this->getPasswordByEmail($email);

                if(password_verify($password, $hashed_password)){
                    return USER_AUTHENTICATED;
                } else{
                    return USER_PASSWORD_DO_NOT_MATCH;
                }
                
            } else{
                return USER_NOT_FOUND;
            }
        }

        public function getAllUsers(){
            $stmt = $this->con->prepare('SELECT id, email, username FROM users');   
            $stmt->execute();
            $stmt->bind_result($id, $email, $username);
            $stmt->fetch();

            $users = array();
            while($stmt->fetch()){
                $user = array();
                $user['id'] = $id;
                $user['email'] = $email;
                $user['username'] = $username;

                array_push($users, $user);
            }

            return $users;
        }

        public function updateUser($id, $email, $username){
            $stmt = $this->con->prepare('UPDATE users SET email = ?, username = ? WHERE id = ?');
            $stmt ->bind_param('ssi', $email, $username, $id);

            if($stmt->execute()){
                return true;
            } else{
                return false;
            }
        }

        public function updatePassword($currentPassword, $newPassword, $email){
            $hashed_password = $this->getPasswordByEmail($email);
            if(password_verify($currentPassword, $hashed_password)){
                $hash_new_password = password_hash($newPassword, PASSWORD_DEFAULT);

                $stmt = $this->con->prepare('UPDATE users SET password = ? WHERE email = ?');
                $stmt ->bind_param('ss', $hash_new_password, $email);

                if($stmt->execute()){
                    return PASSWORD_CHANGED;
                }

                return PASSWORD_NOT_CHANGED;
                
            }

            return PASSWORD_DO_NOT_MATCH;
        }

        public function deleteUser($id){
            $stmt = $this->con->prepare('DELETE FROM users WHERE id = ?');
            $stmt->bind_param('s', $id);

            if($stmt->execute()){
                return true;
            }

            return false;
        }

        private function getPasswordByEmail($email){
            $stmt = $this->con->prepare('SELECT password FROM users WHERE email = ?');
            $stmt->bind_param('s', $email);
            $stmt->execute();
            $stmt->bind_result($password);
            $stmt->fetch();

            return $password;
        }

        public function getUserByEmail($email){
            $stmt = $this->con->prepare('SELECT id, email, username FROM users where email = ?');
            $stmt->bind_param('s', $email);
            $stmt->execute();
            $stmt->bind_result($id, $email, $username);
            $stmt->fetch();

            $user = array();
            $user['id'] = $id;
            $user['email'] = $email;
            $user['username'] = $username;

            return $user;
        }

        public function isEmailExist($email){

            $stmt = $this->con->prepare("SELECT id FROM users WHERE email = ?");
            $stmt->bind_param("s", $email);
            $stmt->execute();
            $stmt->store_result();

            return $stmt->num_rows() > 0; 
        }

        public function isUserExistById($id){
            $stmt = $this->con->prepare('SELECT email FROM users where id = ?');
            $stmt->bind_param('s', $id);
            $stmt->execute();
            $stmt->store_result();

            return $stmt->num_rows() > 0; 
        }
    
        public function createComment($user_id, $rating, $content, $place_id, $pub_date){
            //check if user exists
            if($this->isUserExistById($user_id)){

                $stmt = $this->con->prepare("INSERT INTO comments (user_id, place_id, rating, content, pub_date) VALUES (?, ?, ?, ?, ?)");
                $stmt->bind_param("isdss", $user_id, $place_id, $rating, $content,$pub_date);
            
                if($stmt->execute()){
                    return COMMENT_CREATED;
                } else{
                    return COMMENT_FAILURE;
                }
            }
            return COMMENT_USER_NOT_EXISTS;
        }

        //Only 3 parameters, because lat, lng and place_name are unnecessary.
        public function updateComment($id, $rating, $content){
            $stmt = $this->con->prepare("UPDATE comments SET rating = ?, content = ? WHERE id = ?");
            
            $stmt->bind_param("dsi", $rating, $content, $id);

            if($stmt->execute()){
                return true;
            } else{
                return false;
            }
        }

        public function deleteComment($id){
            $stmt = $this->con->prepare('DELETE FROM comments WHERE id = ?');
            $stmt->bind_param('i', $id);

            if($stmt->execute()){
                return true;
            }

            return false;
        }

        public function getCommentsByUserId($user_id){
            $stmt = $this->con->prepare('SELECT id, place_id, rating, content, pub_date FROM comments where user_id = ?');
            $stmt->bind_param('i', $user_id);
            $stmt->execute();
            $stmt->bind_result($id, $place_id, $rating, $content, $pub_date);

            $comments = array();

            while($stmt->fetch()){
                $comment = array();
                $comment['id'] = $id;
                $comment['user_id'] = $user_id;
                $comment['place_id'] = $place_id;
                $comment['rating'] = $rating;
                $comment['content'] = $content;
                $comment['pub_date'] = $pub_date;

                array_push($comments, $comment);
            }

            return $comments;    
        }

        public function getCommentsByPlaceId($place_id){
            $stmt = $this->con->prepare('SELECT id, user_id, rating, content, pub_date FROM comments where place_id = ?');
            $stmt->bind_param('s', $place_id);
            $stmt->execute();
            $stmt->bind_result($id, $user_id, $rating, $content, $pub_date);

            $comments = array();

            while($stmt->fetch()){
                $comment = array();
                $comment['id'] = $id;
                $comment['user_id'] = $user_id;
                $comment['place_id'] = $place_id;
                $comment['rating'] = $rating;
                $comment['content'] = $content;
                $comment['pub_date'] = $pub_date;

                array_push($comments, $comment);
            }

            return $comments;    
        }

        public function getCommentById($id){
            $stmt = $this->con->prepare('SELECT id, user_id, place_id, rating, content, pub_date FROM comments where id = ?');
            $stmt->bind_param('s', $id);
            $stmt->execute();
            $stmt->bind_result($id, $user_id, $place_id, $rating, $content, $pub_date);
            $stmt->fetch();

            $comment = array();
            $comment['id'] = $id;
            $comment['user_id'] = $user_id;
            $comment['place_id'] = $place_id;
            $comment['rating'] = $rating;
            $comment['content'] = $content;
            $comment['pub_date'] = $pub_date;

            return $comment;
        }

        public function getAllPlaceId(){
            $stmt = $this->con->prepare('SELECT DISTINCT place_id FROM comments');
            $stmt->execute();
            $stmt->bind_result($place_id);

            $places = array();

            while($stmt->fetch()){
                $place = array();
                $place['place_id'] = $place_id;

                array_push($places, $place);
            }

            return $places;
        }


        public function isCommentExistById($id){
            $stmt = $this->con->prepare('SELECT user_id FROM comments where id = ?');
            $stmt->bind_param('s', $id);
            $stmt->execute();
            $stmt->store_result();

            return $stmt->num_rows() > 0; 
        }
    }
?>
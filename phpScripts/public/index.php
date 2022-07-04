<?php
use Psr\Http\Message\ResponseInterface as Response;
use Psr\Http\Message\ServerRequestInterface as Request;
use Slim\Factory\AppFactory;

require __DIR__.'/../vendor/autoload.php';
require __DIR__.'/../include/DbOperations.php';

$app = AppFactory::create();
$app->setBasePath("/BeSafeTogether/public");
$app->addErrorMiddleware(true, true, true);

/*
	endPoint : createUser
	parameters : username, password, email
	method : POST
*/
$app->post('/createuser', function(Request $request, Response $response) {
	$response_code = 422;

	if (!haveEmptyParameters(array('email', 'password', 'username'), $response)) {
		$request_data = $_REQUEST;

		$email = $request_data['email'];
		$password = $request_data['password'];
		$username = $request_data['username'];

		$hash_password = password_hash($password, PASSWORD_DEFAULT);

		$db = new DbOperations;

		$result = $db->createUser($username, $email, $hash_password);
		$message = array();

		if ($result == USER_CREATED) {

			$message['error'] = false;
			$message['message'] = 'User Created Successfully.';
			$response_code= 201;

		} else if ($result == USER_FAILURE) {

			$message['error'] = true;
			$message['message'] = 'Some error occurred.';
			$response_code= 422;

		} else if ($result == USER_EXISTS) {

			$message['error'] = true;
			$message['message'] = 'User Already Exists.';
			$response_code= 422;

		}
		$response->getBody()->write(json_encode($message));

		return $response
			->withHeader('Content-type', 'application/json')
			->withStatus($response_code);
	}

	return $response
		->withHeader('Content-type', 'application/json')
		->withStatus($response_code);

});

$app->post('/loginuser', function(Request $request, Response $response){

	if(!haveEmptyParameters(array('email', 'password'), $response)){
		$request_data = $_REQUEST;

		$email = $request_data['email'];
		$password = $request_data['password'];

		$db = new DbOperations();

		$result = $db->userlogin($email, $password); 
		$message = array();

		if($result == USER_AUTHENTICATED){

			$user = $db->getUserByEmail($email);

			$message['error'] = false;
			$message['message'] = 'Login successful';
			$message['user'] = $user;

		} else if($result == USER_NOT_FOUND){

			$message['error'] = true;
			$message['message'] = 'User not found';

		} else if ($result == USER_PASSWORD_DO_NOT_MATCH){

			$message['error'] = true;
			$message['message'] = 'Wrong password';

		}

		$response->getBody()->write(json_encode($message));

		return $response
								->withHeader('Content-type', 'application/json')
								->withStatus(200);
	}

	return $response
		->withHeader('Content-type', 'application/json')
		->withStatus(422);

});

$app->get('/allusers', function(Request $request, Response $response){

	$db = new DbOperations();

	$users = $db->getAllUsers();

	$message = array();
	$message['error'] = false;
	$message['users'] = $users;

	$response->getBody()->write(json_encode($message));

	return $response
		->withHeader('Content-type', 'application/json')
		->withStatus(200);
});

$app->put('/updateuser/{id}', function(Request $request, Response $response, array $args){
	$id = $args['id'];

	if(!haveEmptyParameters(array('email', 'username'), $response)){
		$request_data = $_REQUEST;

		$email = $request_data['email'];
		$username = $request_data['username'];

		$db = new DbOperations();
		$message = array();

		if($db->updateUser($id, $email, $username)){
			if($db->isUserExistById($id)){
				$user = $db->getUserByEmail($email);

				$message['error'] = false;
				$message['message'] = 'User updated successfully';
				$message['user'] = $user;
			} else{

				$message['error'] = true;
				$message['message'] = 'There is no such user';
			}

			
		} else{
			$user = $db->getUserByEmail($email);

			$message['error'] = false;
			$message['message'] = 'Please try again later';
			$message['user'] = $user;

		}

		$response->getBody()->write(json_encode($message));

		return $response
			->withHeader('Content-type', 'application/json')
			->withStatus(200);
	}

	return $response
		->withHeader('Content-type', 'application/json')
		->withStatus(422);
});

$app->put('/updatepassword', function(Request $request, Response $response){

	if(!haveEmptyParameters(array('current_password', 'new_password', 'email'), $response)){
		$request_data = $_REQUEST;

		$email = $request_data['email'];
		$current_password = $request_data['current_password'];
		$new_password = $request_data['new_password'];

		$db = new DbOperations();
		$message = array();

		$result = $db->updatePassword($current_password, $new_password, $email);

		if($result == PASSWORD_CHANGED){

			$message['error'] = false;
			$message['message'] = 'Password was successfully changed';

		} else if ($result == PASSWORD_DO_NOT_MATCH){

			$message['error'] = true;
			$message['message'] = 'Password is wrong';

		} else if ($result == PASSWORD_NOT_CHANGED){

			$message['error'] = true;
			$message['message'] = 'Password was not changed';

		}

		$response->getBody()->write(json_encode($message));

			return $response
				->withHeader('Content-type', 'application/json')
				->withStatus(200);
	}

	return $response
		->withHeader('Content-type', 'application/json')
		->withStatus(422);
});

$app->delete('/deleteuser/{id}', function (Request $request, Response $response, array $args){
	$id= $args['id'];

	$db = new DbOperations();

	$message = array();

	if($db->deleteUser($id)){

		$message['error'] = false;
		$message['message'] = 'User has been deleted';
	} else{

		$message['error'] = true;
		$message['message'] = 'Please try again later';
	}

	$response->getBody()->write(json_encode($message));

	return $response
		->withHeader('Content-type', 'application/json')
		->withStatus(200);
});

$app->post('/createcomment', function(Request $request, Response $response) {
	$response_code = 422;

	if (!haveEmptyParameters(array('user_id', 'content', 'rating','place_id', 'pub_date'), $response)) {
		$request_data = $_REQUEST;

		$user_id = $request_data['user_id'];
		$rating = $request_data['rating'];
		$content = $request_data['content'];
		$place_id = $request_data['place_id'];
		$pub_date = $request_data['pub_date'];

		$db = new DbOperations;

		$result = $db->createComment($user_id, $rating, $content, $place_id, $pub_date);
		$message = array();

		if ($result == COMMENT_CREATED) {

			$message['error'] = false;
			$message['message'] = 'Comment Created Successfully.';
			$response_code= 201;

		} else if ($result == COMMENT_FAILURE) {

			$message['error'] = true;
			$message['message'] = 'Some error occurred.';
			$response_code= 422;
		} else if ($result == COMMENT_USER_NOT_EXISTS) {

			$message['error'] = true;
			$message['message'] = 'User not exists';
			$response_code= 422;
		}

		$response->getBody()->write(json_encode($message));

		return $response
			->withHeader('Content-type', 'application/json')
			->withStatus($response_code);
	}

	return $response
		->withHeader('Content-type', 'application/json')
		->withStatus($response_code);

});

$app->put('/updatecomment/{id}', function(Request $request, Response $response, array $args) {
	$response_code = 422;
	$id = $args['id'];

	if (!haveEmptyParameters(array('rating', 'content'), $response)) {
		$request_data = $_REQUEST;

		$rating = $request_data['rating'];
		$content = $request_data['content'];

		$db = new DbOperations;

		$message = array();

		if($db->isCommentExistById($id)){
			if ($db->updateComment($id, $rating, $content)) {

				$comment = $db->getCommentById($id);

				$message['error'] = false;
				$message['message'] = 'Comment Updated Succesfully.';
				$message['comment'] = $comment;
				$response_code= 201;
	
			} else {
	
				$message['error'] = true;
				$message['message'] = 'Some error occurred.';
				$response_code= 422;
			} 
	
			$response->getBody()->write(json_encode($message));
	
			return $response
				->withHeader('Content-type', 'application/json')
				->withStatus($response_code);
		}
		$message['error'] = true;
		$message['message'] = 'Comment do not exist.';

		$response->getBody()->write(json_encode($message));

		return $response
			->withHeader('Content-type', 'application/json')
			->withStatus($response_code);
	}

	return $response
		->withHeader('Content-type', 'application/json')
		->withStatus($response_code);

});

$app->delete('/deletecomment/{id}', function (Request $request, Response $response, array $args){
	$id= $args['id'];

	$db = new DbOperations();

	$message = array();

	if($db->deleteComment($id)){

		$message['error'] = false;
		$message['message'] = 'Comment has been deleted';
	} else{

		$message['error'] = true;
		$message['message'] = 'Please try again later';
	}

	$response->getBody()->write(json_encode($message));

	return $response
		->withHeader('Content-type', 'application/json')
		->withStatus(200);
});

$app->get('/getcommentsbyuser/{id}', function(Request $request, Response $response, array $args){
	$user_id = $args["id"];
	$request_data = $_REQUEST;

	$message = array();
	$db = new DbOperations();

	$comments = $db->getCommentsByUserId($user_id);

	if($comments == COMMENTS_NOT_FOUND){
		$message['error'] = false;
		$message['message'] = "No comments found by selected user";

		$response->getBody()->write(json_encode($message));

		return $response
			->withHeader('Content-type', 'application/json')
			->withStatus(200);

	}
	$message['error'] = false;
	$message['message'] = "Good";
	$message['comments'] = $comments;

	$response->getBody()->write(json_encode($message));

	return $response
		->withHeader('Content-type', 'application/json')
		->withStatus(200);
});

$app->get('/getcommentsbyplace/{id}', function(Request $request, Response $response, array $args){
	$place_id = $args["id"];
	$request_data = $_REQUEST;

	$message = array();
	$db = new DbOperations();

	$comments = $db->getCommentsByPlaceId($place_id);

	if($comments == COMMENTS_NOT_FOUND){
		$message['error'] = false;
		$message['message'] = "No comments found by selected user";

		$response->getBody()->write(json_encode($message));

		return $response
			->withHeader('Content-type', 'application/json')
			->withStatus(200);

	}
	$message['error'] = false;
	$message['comments'] = $comments;

	$response->getBody()->write(json_encode($message));

	return $response
		->withHeader('Content-type', 'application/json')
		->withStatus(200);
});

$app->get('/allplaceid',  function(Request $request, Response $response){
	$db = new DbOperations();

	$place_ids = $db->getAllPlaceId();

	$message = array();
	$message['error'] = false;
	$message['place_ids'] = $place_ids;

	$response->getBody()->write(json_encode($message));

	return $response
		->withHeader('Content-type', 'application/json')
		->withStatus(200);
});



function haveEmptyParameters($required_params, $response) {
	$error = false;
	$error_params = '';
	$request_params = $_REQUEST;

	foreach ($required_params as $param) {
		if(!isset($request_params[$param]) || strlen($request_params[$param]) <= 0) {
			$error = true;
			$error_params .= $param . ', ';
  		}
	}

	if($error) {
		$error_detail = array();
  		$error_detail['error'] = true;
  		$error_detail['message'] = 'Required parameters ' . substr($error_params, 0, -2) . ' are either missing or empty';

		$response->getBody()->write(json_encode($error_detail));
	}

	return $error;
}

$app->run();

?>
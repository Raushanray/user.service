package com.lcwr.user.service.services.impl;

import com.lcwr.user.service.entities.Hotel;
import com.lcwr.user.service.entities.Rating;
import com.lcwr.user.service.entities.User;
import com.lcwr.user.service.exceptions.ResourceNotFoundException;
import com.lcwr.user.service.external.services.HotelService;
import com.lcwr.user.service.repositories.UserRepository;
import com.lcwr.user.service.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private HotelService hotelService;

    private Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Override
    public User saveUser(User user) {
        //generate unique userId
        String randomUserId = UUID.randomUUID().toString();
        user.setUserId(randomUserId);
        return userRepository.save(user);

    }

    @Override
    public List<User> getAllUser() {
        //implement RATING SERVICE CALL: USING RestTemplate

        return userRepository.findAll();
    }

    //get single user
    @Override
    public User getUser(String userId) {
        //get the user from database with the help of user repository
        User user = userRepository.findById(userId).orElseThrow(() -> new
                ResourceNotFoundException("User with given Id is  Not Found On Server !! :" + userId));
        //fetch rating of the above the user from the RATING SERVICE
        //http://localhost:8083/ratings/users/c7bca050-e6f5-4508-a764-f08961a3c40b
        Rating[] ratingsOfUsers = restTemplate.getForObject
                ("http://RATING-SERVICE/ratings/users/"+user.getUserId(), Rating[].class);
        logger.info("{} ",ratingsOfUsers);
        List<Rating> ratings = Arrays.stream(ratingsOfUsers).toList();

        List<Rating> ratingList = ratings.stream().map(rating -> {
            //apis call to hotel service to get the hotel
            //http://localhost:8082/hotels/e12d31a4-24e1-49e1-89c7-091289824855
//            ResponseEntity<Hotel> forEntity = restTemplate.getForEntity("http://HOTEL-SERVICE/hotels/"+rating.getHotelId(), Hotel.class);
            Hotel hotel = hotelService.getHotel(rating.getHotelId());
//            logger.info("Response Status Code: {} ", forEntity.getStatusCode());
            //set the hotel to rating **
            rating.setHotel(hotel);
            //return the rating
            return rating;
        }).collect(Collectors.toList());

        user.setRatings(ratingList);

        return user;
    }
}

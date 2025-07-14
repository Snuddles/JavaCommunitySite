package com.jcs.javacommunitysite.service;

import com.jcs.javacommunitysite.dto.community.CommunityDTO;
import com.jcs.javacommunitysite.dto.community.CreateCommunityRequest;
import com.jcs.javacommunitysite.model.Community;
import com.jcs.javacommunitysite.repository.CommunityRepository;
import org.springframework.stereotype.Service;

@Service
public class CommunityService {
    private final CommunityRepository communityRepo;

    public CommunityService(CommunityRepository communityRepo) {
        this.communityRepo = communityRepo;
    }

    public CommunityDTO createCommunity(CreateCommunityRequest request) {
        if(communityRepo.existsByName(request.getName())) {
            throw new IllegalArgumentException("Community name already in use");
        }

        Community community = Community.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();

        Community saved = communityRepo.save(community);

        return CommunityDTO.builder()
                .id(saved.getId())
                .name(saved.getName())
                .description(saved.getDescription())
                .build();
    }

}



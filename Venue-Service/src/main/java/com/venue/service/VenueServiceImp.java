package com.venue.service;
import java.util.*;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.venue.customexception.VenueNotFoundException;
import com.venue.dto.*;
import com.venue.entity.Venue;
import com.venue.enums.Status;
import com.venue.repository.VenueRepository;

@Service
public class VenueServiceImp implements VenueService {
    private final VenueRepository vRepo;
    public VenueServiceImp(VenueRepository vRepo){this.vRepo=vRepo;}

    @Override @Transactional
    public VenueResponseDto addVenue(VenueRequestDto request){
        validate(request);
        Venue v=Venue.builder().build();
        BeanUtils.copyProperties(request,v);
        if(v.getStatus()==null) v.setStatus(Status.ACTIVE);
        return toResponse(vRepo.save(v));
    }
    @Override public VenueResponseDto viewVenue(Long id){return toResponse(vRepo.findById(id).orElseThrow(()->new VenueNotFoundException("Venue not found: "+id)));}
    @Override public List<VenueResponseDto> viewAllVenues(){return vRepo.findAll().stream().map(this::toResponse).toList();}
    @Override @Transactional public String deleteVenue(Long id){
        Venue v=vRepo.findById(id).orElseThrow(()->new VenueNotFoundException("Venue not found: "+id));
        v.setStatus(Status.COMPLETED);vRepo.save(v);return "Venue deactivated: "+id;
    }
    @Override @Transactional public VenueResponseDto updateVenue(VenueRequestDto request,long id){
        validate(request); Venue v=vRepo.findById(id).orElseThrow(()->new VenueNotFoundException("Venue not found: "+id));
        String manager = v.getManagerUsername();
        BeanUtils.copyProperties(request,v);
        v.setVenueId(id);
        if (request.getManagerUsername() == null || request.getManagerUsername().isBlank()) v.setManagerUsername(manager);
        return toResponse(vRepo.save(v));
    }
    private void validate(VenueRequestDto r){
        if(r.getVenueName()==null||r.getVenueName().isBlank())throw new IllegalArgumentException("Venue name is required");
        if(r.getCity()==null||r.getCity().isBlank())throw new IllegalArgumentException("City is required");
        if(r.getCapacity()==null||r.getCapacity()<=0)throw new IllegalArgumentException("Capacity must be greater than zero");
        if(r.getVenueType()==null)throw new IllegalArgumentException("Venue type is required");
        if(r.getFacilities()==null)throw new IllegalArgumentException("Facilities are required");
    }
    private VenueResponseDto toResponse(Venue v){VenueResponseDto r=VenueResponseDto.builder().build();BeanUtils.copyProperties(v,r);return r;}
}

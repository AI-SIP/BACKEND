package com.aisip.OnO.backend.service;

import com.aisip.OnO.backend.Dto.Problem.ProblemRegisterDto;
import com.aisip.OnO.backend.Dto.Problem.ProblemResponseDto;
import com.aisip.OnO.backend.converter.ProblemConverter;
import com.aisip.OnO.backend.entity.Problem;
import com.aisip.OnO.backend.entity.User;
import com.aisip.OnO.backend.repository.ProblemRepository;
import com.aisip.OnO.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ProblemServiceImpl implements ProblemService{

    private final UserRepository userRepository;
    private final ProblemRepository problemRepository;

    @Override
    public ProblemResponseDto saveProblem(Long userId, ProblemRegisterDto problemRegisterDto) {

        Optional<User> optionalUser = userRepository.findById(userId);

        if(optionalUser.isPresent()){
            User user = optionalUser.get();

            Problem problem = Problem.builder()
                    .user(user)
                    .imageUrl(problemRegisterDto.getImageUrl())
                    .processImageUrl("")
                    .solveImageUrl(problemRegisterDto.getSolveImageUrl())
                    .answerImageUrl(problemRegisterDto.getAnswerImageUrl())
                    .reference(problemRegisterDto.getReference())
                    .memo(problemRegisterDto.getMemo())
                    .solvedAt(problemRegisterDto.getSolvedAt())
                    .createdAt(LocalDate.now())
                    .updateAt(LocalDate.now())
                    .build();

            Problem savedProblem = problemRepository.save(problem);

            return ProblemConverter.convertToResponseDto(savedProblem);

        } else{
            return null;
        }
    }

    @Override
    public boolean deleteProblem(Long userId, Long problemId) {
        Optional<Problem> optionalProblem = problemRepository.findById(problemId);
        if(optionalProblem.isPresent()){
            Problem problem = optionalProblem.get();

            if (problem.getUser().getId().equals(userId)) {
                problemRepository.delete(problem);
                return true;
            }
        }

        return false;
    }

    @Override
    public List<ProblemResponseDto> findAllProblemsByUserId(Long userId) {
        Optional<User> user = userRepository.findById(userId);
        return user.map(u -> problemRepository.findByUser(u)
                    .stream()
                    .map(ProblemConverter::convertToResponseDto)
                    .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }
}

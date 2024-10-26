package com.aisip.OnO.backend.service;

import com.aisip.OnO.backend.Dto.Problem.ProblemPracticeRegisterDto;
import com.aisip.OnO.backend.Dto.Problem.ProblemPracticeResponseDto;
import com.aisip.OnO.backend.converter.ProblemPracticeConverter;
import com.aisip.OnO.backend.entity.Problem.Problem;
import com.aisip.OnO.backend.entity.Problem.ProblemPractice;
import com.aisip.OnO.backend.exception.ProblemNotFoundException;
import com.aisip.OnO.backend.exception.ProblemPracticeNotFoundException;
import com.aisip.OnO.backend.repository.ProblemPracticeRepository;
import com.aisip.OnO.backend.repository.ProblemRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ProblemPracticeServiceImpl implements ProblemPracticeService{

    private final ProblemRepository problemRepository;

    private final ProblemPracticeRepository problemPracticeRepository;

    @Override
    public ProblemPractice createProblemPractice(Long userId, ProblemPracticeRegisterDto problemPracticeRegisterDto) {
        ProblemPractice practice = ProblemPractice.builder()
                .practiceCount(0L)
                .build();

        if(problemPracticeRegisterDto.getTitle() != null){
            practice.setTitle(problemPracticeRegisterDto.getTitle());
        }

        if(!problemPracticeRegisterDto.getProblemIds().isEmpty()){
            List<Long> problemIds = problemPracticeRegisterDto.getProblemIds();

            List<Problem> problems = problemIds.stream()
                    .map(problemRepository::findById)  // Optional<Problem> 반환
                    .filter(optionalProblem -> optionalProblem.isPresent() &&
                            optionalProblem.get().getUser().getId().equals(userId))  // 조건을 만족하는 Optional<Problem>만 남김
                    .map(Optional::get)  // Optional<Problem>을 Problem으로 변환
                    .toList();

            practice.setProblems(problems);
        }

        return problemPracticeRepository.save(practice);
    }

    @Override
    public void addProblemToPractice(Long practiceId, Long problemId) {
        ProblemPractice practice = problemPracticeRepository.findById(practiceId)
                .orElseThrow(() -> new ProblemPracticeNotFoundException("Invalid practice practiceId: " + practiceId));

        Problem problem = problemRepository.findById(problemId)
                        .orElseThrow(() -> new ProblemNotFoundException("문제를 찾을 수 없습니다! problemId: " + problemId));

        practice.getProblems().add(problem);
        problemPracticeRepository.save(practice);
    }

    @Override
    public ProblemPracticeResponseDto getPracticeById(Long practiceId) {
        ProblemPractice practice = problemPracticeRepository.findById(practiceId)
                .orElseThrow(() -> new ProblemPracticeNotFoundException("Invalid practice practiceId: " + practiceId));

        return ProblemPracticeConverter.convertToResponseDto(practice, false);
    }

    public List<ProblemPracticeResponseDto> findAllPracticeByUserId(Long userId){
        return null;
    }

    @Override
    public void deletePractice(Long practiceId) {
        problemPracticeRepository.deleteById(practiceId);
    }

    @Override
    public void deleteProblemFromPractice(Long practiceId, Long problemId) {
        ProblemPractice practice = problemPracticeRepository.findById(practiceId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid practice ID"));

        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new ProblemNotFoundException("문제를 찾을 수 없습니다! problemId: " + problemId));

        practice.getProblems().remove(problem);
        problemPracticeRepository.save(practice);
    }

    @Override
    public void deleteProblemFromAllPractice(Long problemId) {
        Optional<Problem> optionalProblem = problemRepository.findById(problemId);

        if (optionalProblem.isPresent()) {
            Problem problemToRemove = optionalProblem.get();

            // 해당 문제를 포함하고 있는 모든 ProblemPractice 가져오기
            List<ProblemPractice> practicesContainingProblem = problemPracticeRepository.findAllByProblemsContaining(problemToRemove);

            for (ProblemPractice practice : practicesContainingProblem) {
                practice.getProblems().remove(problemToRemove);

                problemPracticeRepository.save(practice);
            }

            problemRepository.delete(problemToRemove);
        }
    }
}

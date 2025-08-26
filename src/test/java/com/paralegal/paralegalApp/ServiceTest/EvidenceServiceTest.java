package com.paralegal.paralegalApp.ServiceTest;

import com.paralegal.paralegalApp.Exceptions.EvidenceNotFoundException;
import com.paralegal.paralegalApp.Model.Evidence;
import com.paralegal.paralegalApp.Repository.EvidenceRepository;
import com.paralegal.paralegalApp.Service.EvidenceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EvidenceServiceTest {
    @Mock
    private EvidenceRepository evidenceRepository;
    @InjectMocks
    private EvidenceService evidenceService;

    private Evidence existing;
        @BeforeEach
            void setUp(){
                existing = Evidence.builder()
                        .Id(1L)
                        .fileName("photo.png")
                        .fileType("image/png")
                        .data(new byte[] {1,2,3})
                        .build();
            }
            @Test
            void getAllEvidence_returnListFromRepo(){
            Evidence another = Evidence.builder().Id(2L).fileName("doc.pdf").fileType("application/pdf").build();
            when(evidenceRepository.findAll()).thenReturn(List.of(existing, another));

            var result = evidenceService.getAllEvidence();

            assertThat(result).containsExactly(existing,another);
            verify(evidenceRepository).findAll();
            }

            @Test
            void getEvidenceById_found_returnsOptionalWithValue(){
            when(evidenceRepository.findById(1L)).thenReturn(Optional.of(existing));

            var result = evidenceService.getEvidenceById(1L);

            verify(evidenceRepository).findById(1L);
            assertThat(result).isPresent();
            }
            @Test
            void getEvidenceById_notFound_returnsEmpty(){
            when(evidenceRepository.findById(99L)).thenReturn(Optional.empty());

            var result = evidenceService.getEvidenceById(99L);

            assertThat(result).isEmpty();
            verify(evidenceRepository).findById(99L);
            }
            @Test
            void createEvidence_savesAndReturns(){
            when(evidenceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            var result = evidenceService.createEvidence(existing);

            assertThat(result).isSameAs(existing);
            verify(evidenceRepository).save(existing);
            }
            @Test
            void updateEvidence_found_setsIdAndSaves(){
            when(evidenceRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(evidenceRepository.save(any(Evidence.class))).thenAnswer(inv -> inv.getArgument(0));

            Evidence incoming = Evidence.builder()
                             .Id(999L)
                            .fileName("updated.png")
                            .fileType("image/png")
                            .build();

            var result = evidenceService.updateEvidence(1L, incoming);
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getFileName()).isEqualTo("updated.png");
            verify(evidenceRepository).findById(1L);
            verify(evidenceRepository).save(any(Evidence.class));

            }
            @Test
            void updateEvidence_notFound_throws(){
            when(evidenceRepository.findById(42L)).thenReturn(Optional.empty());

            assertThatThrownBy(()-> evidenceService.updateEvidence(42L,existing))
                    .isInstanceOf(EvidenceNotFoundException.class)
                    .hasMessageContaining("Evidence Not Found");

            verify(evidenceRepository).findById(42L);
            verify(evidenceRepository, never()).save(any());
            }
            @Test
            void partiallyUpdatedEvidence_updatedSimpleFields_andSaves(){
            when(evidenceRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(evidenceRepository.save(any(Evidence.class))).thenAnswer(inv -> inv.getArgument(0));

            Map<String, Object> patch = Map.of(
                    "fileName", "patched.jpg",
                    "fileType", "image/jpeg"
            );

            var patched = evidenceService.partiallyUpdateEvidence(1L,patch);

            assertThat(patched.getFileName()).isEqualTo("patched.jpg");
            assertThat(patched.getFileType()).isEqualTo("image/jpeg");
            verify(evidenceRepository).findById(1L);
            verify(evidenceRepository).save(any(Evidence.class));
            }
    @Test
    void partiallyUpdateEvidence_ignoresUnknownKeys_andStillSavesKnown(){
        when(evidenceRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(evidenceRepository.save(any(Evidence.class))).thenAnswer(inv -> inv.getArgument(0));

        Map<String, Object> patch = Map.of(
                "fileName", "patched.jpg",
                "doesNotExist", "valueToIgnore"
        );

        var patched = evidenceService.partiallyUpdateEvidence(1L, patch);

        assertThat(patched.getFileName()).isEqualTo("patched.jpg");
        verify(evidenceRepository).save(any(Evidence.class));
    }
    @Test
    void partiallyUpdateEvidence_notFound_throws(){
            when(evidenceRepository.findById(123L)).thenReturn(Optional.empty());

            assertThatThrownBy(()-> evidenceService.partiallyUpdateEvidence(123L,Map.of("fileName", "x")))
                    .isInstanceOf(EvidenceNotFoundException.class)
                    .hasMessageContaining("Evidence not Found by id: 123");

            verify(evidenceRepository, never()).save(any());
            verify(evidenceRepository).findById(123L);
    }
    @Test
    void deleteEvidence_delegatesToRepo(){
            evidenceService.deleteEvidence(5L);
            verify(evidenceRepository).deleteById(5L);
    }
}

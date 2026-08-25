package com.minhhao.novelscout.interaction;

import com.minhhao.novelscout.catalog.Novel;
import com.minhhao.novelscout.catalog.NovelRepository;
import com.minhhao.novelscout.user.Role;
import com.minhhao.novelscout.user.RoleName;
import com.minhhao.novelscout.user.RoleRepository;
import com.minhhao.novelscout.user.User;
import com.minhhao.novelscout.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
public class DemoInteractionSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoInteractionSeeder.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final NovelRepository novelRepository;
    private final NovelFavoriteRepository favoriteRepository;
    private final NovelRatingRepository ratingRepository;
    private final UserInteractionRepository interactionRepository;
    private final PasswordEncoder passwordEncoder;

    public DemoInteractionSeeder(UserRepository userRepository,
                                RoleRepository roleRepository,
                                NovelRepository novelRepository,
                                NovelFavoriteRepository favoriteRepository,
                                NovelRatingRepository ratingRepository,
                                UserInteractionRepository interactionRepository,
                                PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.novelRepository = novelRepository;
        this.favoriteRepository = favoriteRepository;
        this.ratingRepository = ratingRepository;
        this.interactionRepository = interactionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (interactionRepository.count() > 0) {
            log.info("Dữ liệu tương tác đã tồn tại ({}), bỏ qua bước Seeder.", interactionRepository.count());
            return;
        }

        List<Novel> novels = novelRepository.findAll();
        if (novels.isEmpty()) {
            log.warn("Chưa có tác phẩm nào trong DB để seed dữ liệu tương tác.");
            return;
        }

        log.info("Bắt đầu khởi tạo dữ liệu giả lập tương tác cho 25 Độc giả...");

        Role readerRole = roleRepository.findByName(RoleName.READER).orElse(null);


        // Sample Vietnamese review comments
        String[] reviewsList = {
                "Tác phẩm tuyệt vời, văn phong rất thấm thía!",
                "Cốt truyện nhân văn, nhân vật được khắc họa sâu sắc.",
                "Đoạn đầu hơi chậm nhưng càng đọc càng cuốn.",
                "Rất đáng đọc, mang đậm giá trị lịch sử và văn hóa.",
                "Giọng văn đượm buồn nhưng rất chân thật.",
                "Một tác phẩm kinh điển không thể bỏ qua.",
                "Nội dung hấp dẫn, tình tiết lôi cuốn từ đầu đến cuối.",
                "Rất ấn tượng với cách xây dựng tâm lý nhân vật.",
                "Đọc đi đọc lại vẫn thấy hay!",
                "Văn phong dung dị mà sâu sắc."
        };

        Random random = new Random(42); // Fixed seed for reproducible realistic data
        List<User> mockUsers = new ArrayList<>();

        // Create 25 mock readers
        for (int i = 1; i <= 25; i++) {
            String username = "reader" + i;
            String email = "reader" + i + "@novelscout.vn";
            String displayName = "Độc Giả " + i;

            User user = userRepository.findByEmailIgnoreCaseOrUsernameIgnoreCase(email, username).orElseGet(() -> {
                User u = User.createReader(email, username, passwordEncoder.encode("password123"), displayName, readerRole);
                return userRepository.save(u);
            });
            mockUsers.add(user);
        }

        int totalRatings = 0;
        int totalFavorites = 0;

        for (User user : mockUsers) {
            // Each user interacts with 8 to 18 random novels
            int interactionCount = 8 + random.nextInt(11);
            List<Novel> shuffledNovels = new ArrayList<>(novels);
            java.util.Collections.shuffle(shuffledNovels, random);

            for (int k = 0; k < Math.min(interactionCount, shuffledNovels.size()); k++) {
                Novel novel = shuffledNovels.get(k);

                // Determine rating score (mostly 3 to 5 stars)
                int score = 3 + random.nextInt(3);
                if (random.nextDouble() < 0.15) score = 1 + random.nextInt(2); // 15% chance low score

                String review = (random.nextDouble() < 0.4) ? reviewsList[random.nextInt(reviewsList.length)] : null;

                if (!ratingRepository.findByUserIdAndNovelId(user.getId(), novel.getId()).isPresent()) {
                    NovelRating rating = new NovelRating(user, novel, score, review);
                    ratingRepository.save(rating);
                    interactionRepository.save(new UserInteraction(user, novel, "RATE", BigDecimal.valueOf(score)));
                    totalRatings++;
                }

                // 40% chance user also favorites the novel
                if (score >= 4 && random.nextDouble() < 0.6) {
                    if (!favoriteRepository.existsByUserIdAndNovelId(user.getId(), novel.getId())) {
                        favoriteRepository.save(new NovelFavorite(user, novel));
                        interactionRepository.save(new UserInteraction(user, novel, "FAVORITE", BigDecimal.valueOf(3.0)));
                        totalFavorites++;
                    }
                }
            }
        }

        // Recalculate average rating & rating count for all novels
        for (Novel novel : novels) {
            Double avgScore = ratingRepository.calculateAverageScoreByNovelId(novel.getId());
            long count = ratingRepository.countByNovelId(novel.getId());
            if (count > 0 && avgScore != null) {
                BigDecimal roundedAvg = BigDecimal.valueOf(avgScore).setScale(2, RoundingMode.HALF_UP);
                updateNovelFields(novel, roundedAvg, count);
            }
        }

        log.info("Hoàn tất seed data tương tác: {} đánh giá sao và {} lượt yêu thích từ {} độc giả giả lập!",
                totalRatings, totalFavorites, mockUsers.size());
    }

    private void updateNovelFields(Novel novel, BigDecimal avgRating, long ratingCount) {
        try {
            var fieldAvg = Novel.class.getDeclaredField("averageRating");
            fieldAvg.setAccessible(true);
            fieldAvg.set(novel, avgRating);

            var fieldCount = Novel.class.getDeclaredField("ratingCount");
            fieldCount.setAccessible(true);
            fieldCount.set(novel, ratingCount);

            novelRepository.save(novel);
        } catch (Exception e) {
            log.error("Lỗi cập nhật điểm trung bình truyện {}: {}", novel.getSlug(), e.getMessage());
        }
    }
}

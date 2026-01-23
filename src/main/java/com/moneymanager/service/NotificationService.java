package com.moneymanager.service;

import com.moneymanager.dto.ExpenseDTO;
import com.moneymanager.entity.ProfileEntity;
import com.moneymanager.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final ProfileRepository profileRepository;
    private final EmailService emailService;
    private final ExpenseService expenseService;

    @Value("${money.manager.frontend.url}")
    private String frontendUrl;

    @Scheduled(cron = "0 0 22 * * *", zone = "IST")
    public void sendDaily_Income_ExpenseReminder() {
        log.info("Job Started : Send Daily Income Expense Reminder ...");
        List<ProfileEntity> allProfiles = profileRepository.findAll();
        for (ProfileEntity profile : allProfiles) {
            String body = "Hi " + profile.getName() + ", <br> <br>"
                    + "This is Friendly reminder to add income and expense for today"
                    + "<a href = " + frontendUrl + " style = 'display:inline-block; padding:10px 20px; background-color:white'> Go To Money Manager </a>"
                    + "<br> <br> Best Regards <br> <br>";
            emailService.sendEmail(profile.getEmail(), "Daily Reminder: add your Income and Expenses", body);
        }
        log.info("Job Completed : Send Daily Income Expense Reminder ...");
    }

    @Scheduled(cron = "0 0 23 * * *", zone = "IST")
    public void sendDaily_ExpenseSummary() {
        log.info("Job Started : sendDaily_ExpenseSummary()");
        List<ProfileEntity> allProfiles = profileRepository.findAll();
        for (ProfileEntity profile : allProfiles) {
            List<ExpenseDTO> todayExpenses = expenseService.getExpenseForUserOnDate(profile.getId(), LocalDate.now(ZoneId.of("Asia/Kolkata")));
            if (!todayExpenses.isEmpty()) {
                StringBuilder table = new StringBuilder();
                table.append("<table style='border-collapse:collapse;width:100%; '>");
                table.append("<tr style='background-color:#f2f2f2; '><th style='border: 1px solid #ddd;padding:8px;'>Category</th><th style='border: 1px sold #ddd; padding: 8px;'> Dates </th> </tr>");
                int i = 1;
                for (ExpenseDTO expenseDTO : todayExpenses) {
                    table.append("<tr>");
                    table.append("<td style = 'border :1px solid #ddd,padding: 8px;'>").append(i++).append("</td>");
                    table.append("<td style = 'border :1px solid #ddd,padding: 8px;'>").append(expenseDTO.getName()).append("</td>");
                    table.append("<td style = 'border :1px solid #ddd,padding: 8px;'>").append(expenseDTO.getAmount()).append("</td>");
                    table.append("<td style = 'border :1px solid #ddd,padding: 8px;'>").append(expenseDTO.getCategoryId() != null ? expenseDTO.getCategoryName() : "N/A").append("</td>");
                    table.append("</tr>");
                }
                table.append("</table>");
                String body = "Hi " + profile.getName() + ", <br> <br> Here is summary of your Income and Expanse for today ... <br> <br> " + table + "<br> <br> Best Regards <br> Money Manager Team";
                emailService.sendEmail(profile.getEmail(), "your daily expense service ...", body);
            }
        }
        log.info("Job Completed  : sendDaily_ExpenseSummary()");
    }

}

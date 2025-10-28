package menus;

import controllers.StudentController;
import models.Book;
import models.BorrowRecord;
import services.BookService;
import services.ReportService;
import utils.InputUtils;
import utils.DateUtils;

import java.time.LocalDate;
import java.util.List;

public class StudentMenu {
    private StudentController controller;
    
    public StudentMenu() {
        this.controller = new StudentController();
    }
    
    public void show() {
        if (!authenticate()) {
            return;
        }
        
        while (true) {
            System.out.println("\n=== منوی دانشجو ===");
            System.out.println("خوش آمدید، " + controller.getCurrentStudent().getFullName());
            System.out.println("1. جستجوی کتاب");
            System.out.println("2. درخواست امانت کتاب");
            System.out.println("3. مشاهده تاریخچه امانت");
            System.out.println("4. مشاهده آمار شخصی");
            System.out.println("0. خروج");
            
            int choice = InputUtils.readIntInput("لطفا گزینه مورد نظر را انتخاب کنید: ");
            
            switch (choice) {
                case 1:
                    searchBooks();
                    break;
                case 2:
                    requestBorrow();
                    break;
                case 3:
                    showBorrowHistory();
                    break;
                case 4:
                    showPersonalStatistics();
                    break;
                case 0:
                    controller.logout();
                    System.out.println("با موفقیت خارج شدید.");
                    return;
                default:
                    System.out.println("گزینه نامعتبر!");
            }
        }
    }
    
    private boolean authenticate() {
        while (true) {
            System.out.println("\n=== ورود دانشجو ===");
            System.out.println("1. ورود");
            System.out.println("2. ثبت نام");
            System.out.println("0. بازگشت به منوی اصلی");
            
            int choice = InputUtils.readIntInput("لطفا گزینه مورد نظر را انتخاب کنید: ");
            
            switch (choice) {
                case 1:
                    return login();
                case 2:
                    return register();
                case 0:
                    return false;
                default:
                    System.out.println("گزینه نامعتبر!");
            }
        }
    }
    
    private boolean login() {
        System.out.println("\n--- ورود به سیستم ---");
        String username = InputUtils.readStringInput("نام کاربری: ");
        String password = InputUtils.readPassword("رمز عبور: ");
        
        if (controller.login(username, password)) {
            System.out.println("✓ ورود موفقیت آمیز!");
            System.out.println("خوش آمدید، " + controller.getCurrentStudent().getFullName());
            return true;
        } else {
            System.out.println("✗ نام کاربری یا رمز عبور اشتباه!");
            return false;
        }
    }
    
    private boolean register() {
        System.out.println("\n--- ثبت نام دانشجو ---");
        String username = InputUtils.readStringInput("نام کاربری: ");
        String password = InputUtils.readPassword("رمز عبور: ");
        String fullName = InputUtils.readStringInput("نام کامل: ");
        String studentId = InputUtils.readStringInput("شماره دانشجویی: ");
        
        if (username.isEmpty() || password.isEmpty() || fullName.isEmpty() || studentId.isEmpty()) {
            System.out.println("✗ تمام فیلدها باید پر شوند!");
            return false;
        }
        
        if (controller.register(username, password, fullName, studentId)) {
            System.out.println("✓ ثبت نام موفقیت آمیز!");
            System.out.println("اکنون می‌توانید با نام کاربری خود وارد شوید.");
            return true;
        } else {
            System.out.println("✗ ثبت نام ناموفق! نام کاربری تکراری است.");
            return false;
        }
    }
    
    private void searchBooks() {
        System.out.println("\n=== جستجوی کتاب ===");
        
        String title = InputUtils.readStringInput("عنوان کتاب (Enter برای نادیده گرفتن): ");
        String author = InputUtils.readStringInput("نام نویسنده (Enter برای نادیده گرفتن): ");
        Integer year = null;
        
        String yearStr = InputUtils.readStringInput("سال انتشار (Enter برای نادیده گرفتن): ");
        if (!yearStr.isEmpty()) {
            try {
                year = Integer.parseInt(yearStr);
            } catch (NumberFormatException e) {
                System.out.println("✗ سال نامعتبر!");
                return;
            }
        }
        
        List<Book> books = controller.searchBooks(
            title.isEmpty() ? null : title,
            author.isEmpty() ? null : author,
            year
        );
        
        if (books.isEmpty()) {
            System.out.println("📚 کتابی یافت نشد.");
        } else {
            System.out.println("\n📖 نتایج جستجو (" + books.size() + " کتاب یافت شد):");
            System.out.println("──────────────────────────────────────────────");
            for (int i = 0; i < books.size(); i++) {
                Book book = books.get(i);
                String status = book.isAvailable() ? "✅ موجود" : "❌ امانت داده شده";
                System.out.printf("%d. %s | نویسنده: %s | سال: %d | وضعیت: %s%n",
                    (i + 1), book.getTitle(), book.getAuthor(), 
                    book.getPublicationYear(), status);
                System.out.printf("   شناسه کتاب: %s | ISBN: %s%n", 
                    book.getBookId(), book.getIsbn());
                System.out.println("──────────────────────────────────────────────");
            }
        }
    }
    
    private void requestBorrow() {
        System.out.println("\n=== درخواست امانت کتاب ===");
        
        // ابتدا کتاب‌های موجود رو نمایش بده
        BookService bookService = BookService.getInstance();
        List<Book> availableBooks = bookService.getAllBooks().stream()
                .filter(Book::isAvailable)
                .toList();
        
        if (availableBooks.isEmpty()) {
            System.out.println("📚 هیچ کتاب موجودی برای امانت وجود ندارد.");
            return;
        }
        
        System.out.println("\n📖 کتاب‌های موجود برای امانت:");
        for (int i = 0; i < availableBooks.size(); i++) {
            Book book = availableBooks.get(i);
            System.out.printf("%d. %s (شناسه: %s)%n", 
                (i + 1), book.getTitle(), book.getBookId());
        }
        
        String bookId = InputUtils.readStringInput("\nشناسه کتاب مورد نظر: ");
        
        // بررسی وجود کتاب
        Book book = bookService.findBookById(bookId);
        if (book == null) {
            System.out.println("✗ کتاب با این شناسه یافت نشد!");
            return;
        }
        
        if (!book.isAvailable()) {
            System.out.println("✗ این کتاب در حال حاضر موجود نیست!");
            return;
        }
        
        // دریافت تاریخ‌ها
        System.out.println("\n📅 لطفا بازه زمانی امانت را مشخص کنید:");
        LocalDate startDate = InputUtils.readDateInput("تاریخ شروع امانت");
        LocalDate endDate = InputUtils.readDateInput("تاریخ پایان امانت");
        
        if (startDate == null || endDate == null) {
            System.out.println("✗ تاریخ‌های وارد شده نامعتبر هستند!");
            return;
        }
        
        if (!DateUtils.isValidBorrowPeriod(startDate, endDate)) {
            System.out.println("✗ بازه زمانی نامعتبر!");
            System.out.println("   - تاریخ شروع باید قبل از تاریخ پایان باشد");
            System.out.println("   - تاریخ پایان نباید قبل از امروز باشد");
            return;
        }
        
        // ثبت درخواست
        if (controller.requestBookBorrow(bookId, startDate, endDate)) {
            System.out.println("✓ درخواست امانت با موفقیت ثبت شد!");
            System.out.println("📋 جزئیات درخواست:");
            System.out.println("   - کتاب: " + book.getTitle());
            System.out.println("   - نویسنده: " + book.getAuthor());
            System.out.println("   - تاریخ شروع: " + startDate);
            System.out.println("   - تاریخ پایان: " + endDate);
            System.out.println("⏳ درخواست شما در انتظار تایید کارمند کتابخانه می‌باشد.");
        } else {
            System.out.println("✗ ثبت درخواست ناموفق! لطفا دوباره تلاش کنید.");
        }
    }
    
    private void showBorrowHistory() {
        System.out.println("\n=== تاریخچه امانت ===");
        
        List<BorrowRecord> history = controller.getBorrowHistory();
        
        if (history == null || history.isEmpty()) {
            System.out.println("📚 هیچ سابقه امانتی یافت نشد.");
            return;
        }
        
        System.out.println("📋 تاریخچه کامل امانت‌های شما:");
        System.out.println("══════════════════════════════════════════════════════");
        
        for (int i = 0; i < history.size(); i++) {
            BorrowRecord record = history.get(i);
            BookService bookService = BookService.getInstance();
            Book book = bookService.findBookById(record.getBookId());
            String bookTitle = (book != null) ? book.getTitle() : "نامشخص";
            
            System.out.printf("%d. کتاب: %s%n", (i + 1), bookTitle);
            System.out.printf("   📅 تاریخ امانت: %s%n", record.getBorrowDate());
            System.out.printf("   📅 تاریخ موعد: %s%n", record.getDueDate());
            
            if (record.isReturned()) {
                System.out.printf("   📅 تاریخ بازگشت: %s%n", record.getReturnDate());
                if (record.isDelayed()) {
                    System.out.println("   ⚠️  وضعیت: با تاخیر بازگردانده شده");
                } else {
                    System.out.println("   ✅ وضعیت: به موقع بازگردانده شده");
                }
            } else {
                if (LocalDate.now().isAfter(record.getDueDate())) {
                    System.out.println("   ❌ وضعیت: در حال تاخیر");
                } else {
                    System.out.println("   🔄 وضعیت: در دست امانت");
                }
            }
            System.out.println("══════════════════════════════════════════════════════");
        }
    }
    
    private void showPersonalStatistics() {
        System.out.println("\n=== آمار شخصی ===");
        
        ReportService.StudentStatistics stats = controller.getStudentStatistics();
        
        if (stats == null) {
            System.out.println("✗ خطا در دریافت آمار!");
            return;
        }
        
        System.out.println("📊 آمار امانت‌های شما:");
        System.out.println("──────────────────────────────────");
        System.out.printf("📚 تعداد کل امانت‌ها: %d%n", stats.totalBorrows);
        System.out.printf("🔄 کتاب‌های در دست امانت: %d%n", stats.notReturnedCount);
        System.out.printf("⏰ امانت‌های با تاخیر: %d%n", stats.delayedReturns);
        
        if (stats.totalBorrows > 0) {
            double delayPercentage = (double) stats.delayedReturns / stats.totalBorrows * 100;
            System.out.printf("📈 درصد تاخیر: %.1f%%%n", delayPercentage);
        }
        
        System.out.println("──────────────────────────────────");
        
        // نمایش آخرین امانت‌ها
        if (stats.borrowHistory != null && !stats.borrowHistory.isEmpty()) {
            System.out.println("\n📋 آخرین امانت‌های شما:");
            System.out.println("══════════════════════════════════════════════════════");
            
            int displayCount = Math.min(5, stats.borrowHistory.size());
            for (int i = 0; i < displayCount; i++) {
                BorrowRecord record = stats.borrowHistory.get(i);
                BookService bookService = BookService.getInstance();
                Book book = bookService.findBookById(record.getBookId());
                String bookTitle = (book != null) ? book.getTitle() : "نامشخص";
                
                System.out.printf("%d. %s%n", (i + 1), bookTitle);
                System.out.printf("   📅 امانت: %s | موعد: %s%n", 
                    record.getBorrowDate(), record.getDueDate());
                
                String status;
                if (record.isReturned()) {
                    status = record.isDelayed() ? "با تاخیر بازگردانده شده" : "به موقع بازگردانده شده";
                } else {
                    status = LocalDate.now().isAfter(record.getDueDate()) ? 
                             "در حال تاخیر" : "در دست امانت";
                }
                System.out.printf("   📊 وضعیت: %s%n", status);
                System.out.println("──────────────────────────────────────────────────────");
            }
            
            if (stats.borrowHistory.size() > 5) {
                System.out.printf("... و %d مورد دیگر%n", stats.borrowHistory.size() - 5);
            }
        }
    }
}
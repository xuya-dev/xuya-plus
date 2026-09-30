package dev.xuya.web.controller.tool;

import com.zaxxer.hikari.HikariDataSource;
import dev.xuya.codegen.CodeGenerator;
import dev.xuya.core.auth.RequiresPerm;
import dev.xuya.core.common.R;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 代码生成后端：表清单 / 生成预览 / 打包下载（复用 quick-dev-codegen 生成内核）
 */
@RestController
@RequestMapping("/tool/codegen")
@RequiredArgsConstructor
public class CodegenController {

    private final DataSource dataSource;

    /**
     * 当前库的表清单（表名 / 注释 / 引擎 / 创建时间）
     */
    @RequiresPerm("sys:gen:list")
    @GetMapping("/tables")
    public R<List<Map<String, Object>>> tables() throws Exception {
        try (Connection conn = dataSource.getConnection();
             var stmt = conn.createStatement();
             var rs = stmt.executeQuery(
                     "SELECT table_name AS tableName, table_comment AS tableComment, "
                     + "engine, create_time AS createTime FROM information_schema.tables "
                     + "WHERE table_schema = DATABASE() AND table_type = 'BASE TABLE' "
                     + "ORDER BY table_name")) {
            List<Map<String, Object>> rows = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("tableName", rs.getString("tableName"));
                row.put("tableComment", rs.getString("tableComment"));
                row.put("engine", rs.getString("engine"));
                row.put("createTime", rs.getString("createTime"));
                rows.add(row);
            }
            return R.ok(rows);
        }
    }

    /**
     * 生成预览：entity / mapper / controller 三段源码
     */
    @RequiresPerm("sys:gen:preview")
    @GetMapping("/preview/{table}")
    public R<Map<String, String>> preview(@PathVariable String table,
                                          @RequestParam(value = "package", defaultValue = "com.xuya.business") String pkg)
            throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            var result = CodeGenerator.generate(conn, table, pkg, "xuya");
            Map<String, String> code = new LinkedHashMap<>();
            code.put("entity", result.entitySource());
            code.put("mapper", result.mapperSource());
            code.put("controller", result.controllerSource());
            return R.ok(code);
        }
    }

    /**
     * 打包下载生成的三件套源码（zip）
     */
    @RequiresPerm("sys:gen:download")
    @GetMapping("/download/{table}")
    public void download(@PathVariable String table,
                         @RequestParam(value = "package", defaultValue = "com.xuya.business") String pkg,
                         HttpServletResponse response) throws Exception {
        Path tmp = Files.createTempDirectory("xuya-gen-");
        try (Connection conn = dataSource.getConnection()) {
            var result = CodeGenerator.generate(conn, table, pkg, "xuya");
            CodeGenerator.writeFiles(result, tmp);
        }
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename=\""
                + URLEncoder.encode(table + "-codegen.zip", "UTF-8") + "\"");
        try (ZipOutputStream zip = new ZipOutputStream(response.getOutputStream())) {
            List<Path> files = new ArrayList<>(Files.walk(tmp)
                    .filter(Files::isRegularFile).sorted(Comparator.naturalOrder()).toList());
            for (Path file : files) {
                zip.putNextEntry(new ZipEntry(tmp.relativize(file).toString().replace('\\', '/')));
                Files.copy(file, zip);
                zip.closeEntry();
            }
        } finally {
            deleteRecursively(tmp);
        }
    }

    private void deleteRecursively(Path path) throws IOException {
        if (path == null || !Files.exists(path)) {
            return;
        }
        try (var walk = Files.walk(path)) {
            walk.sorted(Comparator.reverseOrder()).forEach(file -> {
                try {
                    Files.delete(file);
                } catch (IOException ignored) {
                }
            });
        }
    }
}

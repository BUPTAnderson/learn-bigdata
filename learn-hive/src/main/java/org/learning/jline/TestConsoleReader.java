package org.learning.jline;

import com.google.common.base.Splitter;
import jline.console.ConsoleReader;
import jline.console.completer.ArgumentCompleter;
import jline.console.completer.Completer;
import jline.console.completer.StringsCompleter;
import jline.console.history.FileHistory;
import jline.console.history.History;
import jline.console.history.PersistentHistory;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.conf.HiveVariableSource;
import org.apache.hadoop.hive.conf.Validator;
import org.apache.hadoop.hive.conf.VariableSubstitution;
import org.apache.hadoop.hive.ql.exec.FunctionRegistry;
import org.apache.hadoop.hive.ql.parse.HiveParser;
import org.apache.hadoop.hive.ql.session.SessionState;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by anderson on 17-7-12.
 */
public class TestConsoleReader
{
    protected ConsoleReader reader;
    public static final int DELIMITED_CANDIDATE_THRESHOLD = 10;

    protected void setupConsoleReader() throws IOException
    {
        reader = new ConsoleReader();
        reader.setExpandEvents(false);
        reader.setBellEnabled(false);
        //  JLine中跟自动补全相关的接口是Completer, 每个completer代表一类自动补全规则, 添加completer的顺序就是调用自动补全规则的顺序
        for (Completer completer : getCommandCompleter()) {
            // 将各种自动补全的completer添加到reader
            reader.addCompleter(completer);
        }
        // 可以通过ConsoleReader的setUseHistory(boolean useHistory)方法启用/禁用Command History功能。
        // ConsoleReader的history成员变量负责保存历史数据，默认情况下历史数据只保存在内存中。如果希望将历史数据保存 到文件中，那么只需要以File对象作为参数构造History对象，并将该History对象设置到ConsoleReader即可。
        setupCmdHistory();
    }

    public static Completer[] getCommandCompleter() {
        // StringsCompleter与预定义的单词列表匹配. 我们从一个空白的单词开始，并构建它
        // StringsCompleter matches against a pre-defined wordlist
        // We start with an empty wordlist and build it up
        List<String> candidateStrings = new ArrayList<String>();

        // 添加函数, 如果函数只包含字母和下划线则在函数名最后添加一个'('
        // We add Hive function names
        // For functions that aren't infix operators, we add an open
        // parenthesis at the end.
        for (String s : FunctionRegistry.getFunctionNames()) {
            if (s.matches("[a-z_]+")) {
                candidateStrings.add(s + "(");
            } else {
                candidateStrings.add(s);
            }
        }

        // 添加关键词(包括大小写), 如: PARTITION, UPDATE, ALTER
        // We add Hive keywords, including lower-cased versions
        for (String s : HiveParser.getKeywords()) {
            candidateStrings.add(s);
            candidateStrings.add(s.toLowerCase());
        }

        StringsCompleter strCompleter = new StringsCompleter(candidateStrings);

        // 因为除了空格以外我们还使用括号作为关键字分隔符，我们需要定义一个新的ArgumentDelimiter，它将'(', ')', '[', ']', ' '识别为分隔符。
        // Because we use parentheses in addition to whitespace
        // as a keyword delimiter, we need to define a new ArgumentDelimiter
        // that recognizes parenthesis as a delimiter.
        ArgumentCompleter.ArgumentDelimiter delim = new ArgumentCompleter.AbstractArgumentDelimiter() {
            @Override
            public boolean isDelimiterChar(CharSequence buffer, int pos) {
                char c = buffer.charAt(pos);
                return (Character.isWhitespace(c) || c == '(' || c == ')' ||
                        c == '[' || c == ']');
            }
        };

        // The ArgumentCompletor allows us to match multiple tokens
        // in the same line.
        final ArgumentCompleter argCompleter = new ArgumentCompleter(delim, strCompleter);
        // By default ArgumentCompletor is in "strict" mode meaning
        // a token is only auto-completed if all prior tokens
        // match. We don't want that since there are valid tokens
        // that are not in our wordlist (eg. table and column names)
        argCompleter.setStrict(false);

        // ArgumentCompletor总是在匹配的令牌之后添加一个空格。 这对于函数名称是不希望的，因为开头括号后的空格在Hive中是不必要的（并且不常见）。 我们将自定义Completor放在我们的ArgumentCompletor之上，以扭转这一点。
        // ArgumentCompletor always adds a space after a matched token.
        // This is undesirable for function names because a space after
        // the opening parenthesis is unnecessary (and uncommon) in Hive.
        // We stack a custom Completor on top of our ArgumentCompletor
        // to reverse this.
        Completer customCompletor = new Completer() {
            @Override
            public int complete(String buffer, int offset, List completions) {
                List<String> comp = completions;
                int ret = argCompleter.complete(buffer, offset, completions);
                // ConsoleReader will do the substitution if and only if there
                // is exactly one valid completion, so we ignore other cases.
                if (completions.size() == 1) {
                    if (comp.get(0).endsWith("( ")) {
                        comp.set(0, comp.get(0).trim());
                    }
                }
                return ret;
            }
        };

        List<String> vars = new ArrayList<String>();
        // 获取HiveConf所有的配置项, 如: hive.jar.path
        for (HiveConf.ConfVars conf : HiveConf.ConfVars.values()) {
            vars.add(conf.varname);
        }

        StringsCompleter confCompleter = new StringsCompleter(vars) {
            @Override
            public int complete(final String buffer, final int cursor, final List<CharSequence> clist) {
                int result = super.complete(buffer, cursor, clist);
                if (clist.isEmpty() && cursor > 1 && buffer.charAt(cursor - 1) == '=') {
                    HiveConf.ConfVars var = HiveConf.getConfVars(buffer.substring(0, cursor - 1));
                    if (var == null) {
                        return result;
                    }
                    if (var.getValidator() instanceof Validator.StringSet) {
                        Validator.StringSet validator = (Validator.StringSet) var.getValidator();
                        clist.addAll(validator.getExpected());
                    } else if (var.getValidator() != null) {
                        clist.addAll(Arrays.asList(var.getValidator().toDescription(), ""));
                    } else {
                        clist.addAll(Arrays.asList("Expects " + var.typeString() + " type value", ""));
                    }
                    return cursor;
                }
                if (clist.size() > DELIMITED_CANDIDATE_THRESHOLD) {
                    Set<CharSequence> delimited = new LinkedHashSet<CharSequence>();
                    for (CharSequence candidate : clist) {
                        Iterator<String> it = Splitter.on(".").split(
                                candidate.subSequence(cursor, candidate.length())).iterator();
                        if (it.hasNext()) {
                            String next = it.next();
                            if (next.isEmpty()) {
                                next = ".";
                            }
                            candidate = buffer != null ? buffer.substring(0, cursor) + next : next;
                        }
                        delimited.add(candidate);
                    }
                    clist.clear();
                    clist.addAll(delimited);
                }
                return result;
            }
        };

        StringsCompleter setCompleter = new StringsCompleter("set") {
            @Override
            public int complete(String buffer, int cursor, List<CharSequence> clist) {
                return buffer != null && buffer.equals("set") ? super.complete(buffer, cursor, clist) : -1;
            }
        };

        ArgumentCompleter propCompleter = new ArgumentCompleter(setCompleter, confCompleter) {
            @Override
            public int complete(String buffer, int offset, List<CharSequence> completions) {
                int ret = super.complete(buffer, offset, completions);
                if (completions.size() == 1) {
                    completions.set(0, ((String) completions.get(0)).trim());
                }
                return ret;
            }
        };
        return new Completer[] {propCompleter, customCompletor};
    }

    private void setupCmdHistory() {
        final String historyfile = ".hivehistory";
        String historyDirectory = System.getProperty("user.home");
        PersistentHistory history = null;
        try {
            if ((new File(historyDirectory)).exists()) {
                // 创建文件 /home/username/.hivehistory 用来保存历史查询记录
                String historyFile = historyDirectory + File.separator + historyfile;
                history = new FileHistory(new File(historyFile));
                reader.setHistory(history);
            } else {
                System.err.println("WARNING: Directory for Hive history file: " + historyDirectory +
                        " does not exist.   History will not be available during this session.");
            }
        } catch (Exception e) {
            System.err.println("WARNING: Encountered an error while trying to initialize Hive's " +
                    "history file.  History will not be available during this session.");
            System.err.println(e.getMessage());
        }

        // 增加一个刷新的钩子程序, 关闭的时候将内存中的查询历史刷新到.hivehistory文件中
        // add shutdown hook to flush the history to history file
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                History h = reader.getHistory();
                if (h instanceof FileHistory) {
                    try {
                        ((FileHistory) h).flush();
                    } catch (IOException e) {
                        System.err.println("WARNING: Failed to write command history file: " + e.getMessage());
                    }
                }
            }
        }));
    }

    private static String spacesForString(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        return String.format("%1$-" + s.length() + "s", "");
    }

    public void process()
            throws IOException
    {
        HiveConf conf = new HiveConf();
        setupConsoleReader();
        String prompt = conf.getVar(HiveConf.ConfVars.CLIPROMPT);
        prompt = new VariableSubstitution(new HiveVariableSource() {
            @Override
            public Map<String, String> getHiveVariable() {
                return SessionState.get().getHiveVariables();
            }
        }).substitute(conf, prompt);
        String prompt2 = spacesForString(prompt);

        String line;
        int ret = 0;
        String prefix = "";
        String curDB = "";
        String curPrompt = prompt + curDB;
        // 返回与curDB长度相同的空格
        String dbSpaces = spacesForString(curDB);

        // 终端输出提示符: curPrompt + "> ", 通常是: hive>
        while ((line = reader.readLine(curPrompt + "> ")) != null) {
            if (!prefix.equals("")) {
                prefix += '\n';
            }
            if (line.trim().startsWith("--")) {
                continue;
            }
            if (line.trim().endsWith(";") && !line.trim().endsWith("\\;")) {
                line = prefix + line;
                System.out.println("-->" + line + "<--");
                prefix = "";
                curDB = getFormattedDb(conf);
                curPrompt = prompt + curDB;
                dbSpaces = dbSpaces.length() == curDB.length() ? dbSpaces : spacesForString(curDB);
            } else {
                prefix = prefix + line;
                curPrompt = prompt2 + dbSpaces;
                continue;
            }
        }
    }

    private static String getFormattedDb(HiveConf conf) {
        if (!HiveConf.getBoolVar(conf, HiveConf.ConfVars.CLIPRINTCURRENTDB)) {
            return "";
        }
        //BUG: This will not work in remote mode - HIVE-5153
        String currDb = SessionState.get().getCurrentDatabase();

        if (currDb == null) {
            return "";
        }

        return " (" + currDb + ")";
    }

    public static void main(String[] args)
            throws IOException
    {
        TestConsoleReader reader = new TestConsoleReader();
        reader.process();
    }
}

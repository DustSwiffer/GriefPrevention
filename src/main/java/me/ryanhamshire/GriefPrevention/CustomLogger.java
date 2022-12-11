package me.ryanhamshire.GriefPrevention;

import com.google.common.io.Files;
import me.ryanhamshire.GriefPrevention.enums.CustomLogEntryTypes;
import org.bukkit.scheduler.BukkitScheduler;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class CustomLogger
{
    private final SimpleDateFormat timestampFormat = new SimpleDateFormat("HH:mm");
    private final SimpleDateFormat filenameFormat = new SimpleDateFormat("yyyy_MM_dd");
    private final String logFolderPath = DataStore.dataLayerFolderPath + File.separator + "Logs";

    //stringbuilder is not thread safe, stringbuffer is
    private final StringBuffer queuedEntries = new StringBuffer();

    public CustomLogger()
    {
        //ensure log folder exists
        File logFolder = new File(this.logFolderPath);
        boolean directoryExisting = logFolder.exists();
        if (!directoryExisting)
        {
            directoryExisting = logFolder.mkdirs();
        }

        if (directoryExisting)
        {
            //delete any outdated log files immediately
            this.DeleteExpiredLogs();

            //unless disabled, schedule recurring tasks
            int daysToKeepLogs = GriefPrevention.instance.config_logs_daysToKeep;
            if (daysToKeepLogs > 0)
            {
                BukkitScheduler scheduler = GriefPrevention.instance.getServer().getScheduler();
                final long ticksPerSecond = 20L;
                final long ticksPerDay = ticksPerSecond * 60 * 60 * 24;
                int secondsBetweenWrites = 300;
                scheduler.runTaskTimerAsynchronously(GriefPrevention.instance, new EntryWriter(), secondsBetweenWrites * ticksPerSecond, secondsBetweenWrites * ticksPerSecond);
                scheduler.runTaskTimerAsynchronously(GriefPrevention.instance, new ExpiredLogRemover(), ticksPerDay, ticksPerDay);
            }
        }
    }

    private static final Pattern inlineFormatterPattern = Pattern.compile("§.");

    void AddEntry(String entry, CustomLogEntryTypes entryType)
    {
        //if disabled, do nothing
        int daysToKeepLogs = GriefPrevention.instance.config_logs_daysToKeep;
        if (daysToKeepLogs == 0) return;

        //if entry type is not enabled, do nothing
        if (!this.isEnabledType(entryType)) return;

        //otherwise write to the in-memory buffer, after removing formatters
        Matcher matcher = inlineFormatterPattern.matcher(entry);
        entry = matcher.replaceAll("");
        String timestamp = this.timestampFormat.format(new Date());
        this.queuedEntries.append(timestamp).append(' ').append(entry).append('\n');
    }

    private boolean isEnabledType(CustomLogEntryTypes entryType)
    {
        if (entryType == CustomLogEntryTypes.Exception) return true;
        if (entryType == CustomLogEntryTypes.SocialActivity && !GriefPrevention.instance.config_logs_socialEnabled)
            return false;
        if (entryType == CustomLogEntryTypes.SuspiciousActivity && !GriefPrevention.instance.config_logs_suspiciousEnabled)
            return false;
        if (entryType == CustomLogEntryTypes.AdminActivity && !GriefPrevention.instance.config_logs_adminEnabled)
            return false;
        if (entryType == CustomLogEntryTypes.Debug && !GriefPrevention.instance.config_logs_debugEnabled) return false;
        return entryType != CustomLogEntryTypes.MutedChat || GriefPrevention.instance.config_logs_mutedChatEnabled;
    }

    void WriteEntries()
    {
        try
        {
            //if nothing to write, stop here
            if (this.queuedEntries.length() == 0) return;

            //determine filename based on date
            String filename = this.filenameFormat.format(new Date()) + ".log";
            String filepath = this.logFolderPath + File.separator + filename;
            File logFile = new File(filepath);

            //dump content
            Files.asCharSink(logFile, StandardCharsets.UTF_8).write(this.queuedEntries.toString());

            //in case of a failure to write the above due to exception,
            //the unwritten entries will remain the buffer for the next write to retry
            this.queuedEntries.setLength(0);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void DeleteExpiredLogs()
    {
        try
        {
            //get list of log files
            File logFolder = new File(this.logFolderPath);
            File[] files = logFolder.listFiles();

            //delete any created before x days ago
            int daysToKeepLogs = GriefPrevention.instance.config_logs_daysToKeep;
            Calendar expirationBoundary = Calendar.getInstance();
            expirationBoundary.add(Calendar.DATE, -daysToKeepLogs);
            if (files != null)
            {
                for (File file : files)
                {
                    if (file.isDirectory()) continue;  //skip any folders

                    String filename = file.getName().replace(".log", "");
                    String[] dateParts = filename.split("_");  //format is yyyy_MM_dd
                    if (dateParts.length != 3) continue;

                    try
                    {
                        Calendar filedate = Calendar.getInstance();
                        filedate.set(filedate.get(Calendar.YEAR), filedate.get(Calendar.MONTH), filedate.get(Calendar.DAY_OF_MONTH));
                        if (filedate.before(expirationBoundary))
                        {
                            boolean fileExisting = file.exists();
                            boolean fileDeleted = false;

                            if (fileExisting)
                            {
                                fileDeleted = file.delete();
                            }
                            if (!fileDeleted)
                            {
                                return;

                            }
                        }
                    }
                    catch (NumberFormatException e)
                    {
                        //throw this away - effectively ignoring any files without the correct filename format
                        GriefPrevention.AddLogEntry("Ignoring an unexpected file in the abridged logs folder: " + file.getName(), CustomLogEntryTypes.Debug, true);
                    }
                }
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    //transfers the internal buffer to a log file
    private class EntryWriter implements Runnable
    {
        @Override
        public void run()
        {
            WriteEntries();
        }
    }

    private class ExpiredLogRemover implements Runnable
    {
        @Override
        public void run()
        {
            DeleteExpiredLogs();
        }
    }
}

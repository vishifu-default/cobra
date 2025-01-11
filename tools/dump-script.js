// Define the task to be executed
function doWork() {
    try {
        console.log("Performing the scheduled task...");
        // Your task logic here
    } catch (error) {
        console.error("Error during task execution:", error);
    }
}

// Set the interval to 1.5 minutes (90 seconds)
const interval = 90 * 1000; // Convert minutes to milliseconds

// Schedule the task to run at the specified interval
setInterval(doWork, interval);

// Optionally, you can run the task immediately before the interval starts
doWork();


console.log("Script is running. Press Ctrl+C to stop.");
process.on("SIGINT", () => {
    console.log("Shutting down gracefully...");
    process.exit(0);
});
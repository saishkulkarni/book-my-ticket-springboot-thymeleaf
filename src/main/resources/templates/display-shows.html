<!DOCTYPE html>
<html lang="en" xmlns:th="http://www.thymeleaf.org">

<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Select Show Date | BookMyTicket</title>

<link rel="icon" type="image/svg+xml" href="/favicon.svg">

<link
	href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css"
	rel="stylesheet">
<link
	href="https://fonts.googleapis.com/css2?family=Poppins:wght@400;500;600;700;900&display=swap"
	rel="stylesheet">

<style>
:root {
	--bg-main: #0f172a;
	--bg-card: #111827;
	--border-soft: #1f2937;
	--text-main: #e5e7eb;
	--text-muted: #9ca3af;
	--accent: #e74c3c;
}

* {
	box-sizing: border-box;
}

body {
	font-family: "Poppins", sans-serif;
	background: radial-gradient(circle at top, #1e293b, var(--bg-main));
	min-height: 100vh;
	color: var(--text-main);
}

/* Back */
.back-btn {
	color: var(--text-muted);
	text-decoration: none;
	font-weight: 600;
	transition: color 0.2s ease;
}

.back-btn:hover {
	color: var(--accent);
}

/* Movie Card */
.movie-card {
	background: var(--bg-card);
	border-radius: 18px;
	overflow: hidden;
	border: 1px solid var(--border-soft);
	box-shadow: 0 25px 50px rgba(0, 0, 0, 0.55);
}

.movie-poster {
	width: 100%;
	height: 420px;
	object-fit: cover;
}

/* Movie Details */
.movie-title {
	font-size: 2rem;
	font-weight: 900;
	margin-bottom: 10px;
}

.movie-meta {
	display: flex;
	gap: 10px;
	flex-wrap: wrap;
	margin-bottom: 15px;
}

.badge {
	background: linear-gradient(135deg, #e74c3c, #ff6b6b);
	border-radius: 20px;
	padding: 6px 14px;
	font-size: 0.8rem;
	font-weight: 700;
}

/* Description */
.movie-desc {
	color: var(--text-muted);
	line-height: 1.6;
}

/* Date Picker */
.date-btn {
	background: transparent;
	border: 2px solid var(--accent);
	color: var(--accent);
	font-weight: 700;
	border-radius: 12px;
	padding: 12px 20px;
	transition: all 0.25s ease;
	letter-spacing: 0.3px;
}

.date-btn:hover {
	background: var(--accent);
	color: white;
	transform: translateY(-2px);
	box-shadow: 0 10px 30px rgba(231, 76, 60, 0.5);
}

.date-btn.active {
	background: var(--accent);
	color: white;
	box-shadow: 0 12px 35px rgba(231, 76, 60, 0.6);
}

/* Alerts */
.alert-warning {
	background: #1f2937;
	border: 1px solid #374151;
	color: #fbbf24;
	font-weight: 600;
}

/* Animation */
.fade-in {
	animation: fadeInUp 0.6s ease;
}

@keyframes fadeInUp {from { opacity:0;
	transform: translateY(20px);
}

to {
	opacity: 1;
	transform: translateY(0);
}

}
@media ( max-width : 768px) {
	.movie-poster {
		height: 340px;
	}
	.movie-title {
		font-size: 1.7rem;
	}
}
</style>
</head>

<body>

	<div class="container py-5 fade-in">

		<!-- Back -->
		<a href="/" class="back-btn mb-4 d-inline-block">← Back to Movies</a>

		<!-- Movie Info -->
		<div class="row g-4 mb-5" th:if="${movie != null}">
			<div class="col-md-4">
				<div class="movie-card">
					<img th:src="${movie.imageLink}" class="movie-poster">
				</div>
			</div>

			<div class="col-md-8">
				<h1 class="movie-title" th:text="${movie.name}"></h1>

				<div class="movie-meta">
					<span class="badge" th:text="${movie.genre}"></span> <span
						class="badge" th:text="${movie.languages}"></span>
				</div>

				<p class="text-muted mb-2">
					<strong>Duration:</strong> <span th:text="${movie.duration}"></span>
					hours
				</p>

				<p class="text-muted mb-3">
					<strong>Release:</strong> <span
						th:text="${#temporals.format(movie.releaseDate,'dd MMM yyyy')}"></span>
				</p>

				<p class="movie-desc" th:text="${movie.description}"></p>
			</div>
		</div>

		<!-- No Shows -->
		<div class="alert alert-warning text-center"
			th:if="${showDate == null or showDate.size() == 0}">No shows
			available for this movie.</div>

		<!-- Date Selection -->
		<div th:if="${showDate != null and showDate.size() > 0}">
			<h3 class="fw-bold mb-4 text-center">Select Show Date</h3>

			<div class="d-flex flex-wrap gap-3 justify-content-center"
				id="datePicker"></div>
		</div>

	</div>

	<script th:inline="javascript">
	const showDates = /*[[${showDate}]]*/ [];
	const movieId = /*[[${movie != null ? movie.id : 0}]]*/ 0;

	function formatDate(dateStr) {
		const date = new Date(dateStr);
		return date.toLocaleDateString('en-IN', {
			weekday: 'short',
			day: 'numeric',
			month: 'short'
		});
	}

	function loadDates() {
		const container = document.getElementById('datePicker');
		if (!container || showDates.length === 0) return;

		showDates
			.sort((a, b) => new Date(a) - new Date(b))
			.forEach((date, index) => {
				const btn = document.createElement('button');
				btn.className = 'date-btn' + (index === 0 ? ' active' : '');
				btn.textContent = formatDate(date);

				btn.onclick = () => {
					document.querySelectorAll('.date-btn')
						.forEach(b => b.classList.remove('active'));
					btn.classList.add('active');

					window.location.href =
						`/selectShows?movieId=${movieId}&date=${date}`;
				};

				container.appendChild(btn);
			});
	}

	document.addEventListener('DOMContentLoaded', loadDates);
</script>

	<script
		src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>

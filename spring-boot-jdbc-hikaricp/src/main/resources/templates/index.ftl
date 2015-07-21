<!DOCTYPE html>

<html lang="en">

<body>
	<table>
		<thead>
			<tr>
				<td>Title</td>
				<td>Body</td>
			</tr>
		</thead>
		<tbody>
<#list notes as note>
			<tr>
				<td>${note.title}</td>
				<td>${note.body}</td>
			</tr>
</#list>
		</tbody>
	</table>
</body>

</html>

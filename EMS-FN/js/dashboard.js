// Load employees on page load
$(document).ready(function() {
    $.ajax({
        url: 'http://localhost:8084/EMS_Web_exploded/employee',
        method: 'GET',
        success: function (response) {
            var tbody = $('#employee-table tbody');
            tbody.empty();
            
            response.data.forEach(function(employee) {
                tbody.append(
                    '<tr>' +
                    '<td>' + employee.eid + '</td>' +
                    '<td>' + employee.ename + '</td>' +
                    '<td>' + employee.enumber + '</td>' +
                    '<td>' + employee.eaddress + '</td>' +
                    '<td>' + employee.edepartment + '</td>' +
                    '<td>' + employee.estatus + '</td>' +
                    '</tr>'
                );
            });
        },
        error: function (error) {
            console.log(error);
        }
    });
});

// Save employee
$('#save-employee').on('click', function () {
    var name = $('#ename').val();
    var number = $('#enumber').val();
    var department = $('#edepartment').val();
    var status = $('#estatus').val();
    var address = $('#eaddress').val();
    
    $.ajax({
        method: 'POST',
        url: 'http://localhost:8084/EMS_Web_exploded/employee',
        contentType: 'application/json',
        data: JSON.stringify({
            ename: name,
            enumber: number,
            eaddress: address,
            edepartment: department,
            estatus: status
        }),
        success: function (response) {
            console.log(response);
            if (response.code === '200') {
                alert('Employee saved successfully!');
                $.ajax({
                    url: 'http://localhost:8084/EMS_Web_exploded/employee',
                    method: 'GET',
                    success: function (response) {
                        var tbody = $('#employee-table tbody');
                        tbody.empty();
                        
                        response.data.forEach(function(employee) {
                            tbody.append(
                                '<tr>' +
                                '<td>' + employee.eid + '</td>' +
                                '<td>' + employee.ename + '</td>' +
                                '<td>' + employee.enumber + '</td>' +
                                '<td>' + employee.eaddress + '</td>' +
                                '<td>' + employee.edepartment + '</td>' +
                                '<td>' + employee.estatus + '</td>' +
                                '</tr>'
                            );
                        });
                    },
                    error: function (error) {
                        console.log(error);
                    }
                });
            } else {
                alert('Error: ' + response.message);
            }
        },
        error: function (error) {
            console.log(error);
            alert('Failed to save employee!');
        }
    });
});






//     function fetchEmployees() {
//         $.ajax({
//             method: 'GET',
//             url: 'http://localhost:8084/EMS_Web_exploded/employee',
//             success: function(response) {
//                 if (response.code === '200') {
//                     var employees = response.data;
//                     var employeeTable = $('#employee-table tbody');
//                     employeeTable.empty(); 
//                     employees.forEach(function(employee) {
//                         employeeTable.append(
//                             `<tr>
//                                 <td>
//                                     <button class="btn btn-primary" onclick="editEmployee(${employee.eid})">Edit</button>
//                                     <button class="btn btn-danger" onclick="deleteEmployee(${employee.eid})">Delete</button>
//                                 </td>
//                                 <td>${employee.ename}</td>
//                                 <td>${employee.enumber}</td>
//                                 <td>${employee.eaddress}</td>
//                                 <td>${employee.edepartment}</td>
//                                 <td>${employee.estatus}</td>
                                
//                             </tr>`
//                         );
//                     });
//                 } else {
//                     alert('Error fetching employees: ' + response.message);
//                 }
//             },
//             error: function() {
//                 alert('Failed to fetch employees.');
//             }
//         });
//     }
// });
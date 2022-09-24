import React, { useEffect } from 'react';
import { Link, RouteComponentProps } from 'react-router-dom';
import { Button, Row, Col } from 'reactstrap';
import { Translate, TextFormat } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT, APP_LOCAL_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './todo.reducer';

export const TodoDetail = (props: RouteComponentProps<{ id: string }>) => {
  const dispatch = useAppDispatch();

  useEffect(() => {
    dispatch(getEntity(props.match.params.id));
  }, []);

  const todoEntity = useAppSelector(state => state.todo.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="todoDetailsHeading">
          <Translate contentKey="austinApp.todo.detail.title">Todo</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{todoEntity.id}</dd>
          <dt>
            <span id="task">
              <Translate contentKey="austinApp.todo.task">Task</Translate>
            </span>
          </dt>
          <dd>{todoEntity.task}</dd>
          <dt>
            <span id="scheduledTime">
              <Translate contentKey="austinApp.todo.scheduledTime">Scheduled Time</Translate>
            </span>
          </dt>
          <dd>{todoEntity.scheduledTime}</dd>
          <dt>
            <span id="validUntil">
              <Translate contentKey="austinApp.todo.validUntil">Valid Until</Translate>
            </span>
          </dt>
          <dd>{todoEntity.validUntil ? <TextFormat value={todoEntity.validUntil} type="date" format={APP_DATE_FORMAT} /> : null}</dd>
          <dt>
            <span id="createdDate">
              <Translate contentKey="austinApp.todo.createdDate">Created Date</Translate>
            </span>
          </dt>
          <dd>{todoEntity.createdDate ? <TextFormat value={todoEntity.createdDate} type="date" format={APP_DATE_FORMAT} /> : null}</dd>
          <dt>
            <span id="lastModifiedDate">
              <Translate contentKey="austinApp.todo.lastModifiedDate">Last Modified Date</Translate>
            </span>
          </dt>
          <dd>
            {todoEntity.lastModifiedDate ? <TextFormat value={todoEntity.lastModifiedDate} type="date" format={APP_DATE_FORMAT} /> : null}
          </dd>
          <dt>
            <span id="createdBy">
              <Translate contentKey="austinApp.todo.createdBy">Created By</Translate>
            </span>
          </dt>
          <dd>{todoEntity.createdBy}</dd>
          <dt>
            <span id="lastModifiedBy">
              <Translate contentKey="austinApp.todo.lastModifiedBy">Last Modified By</Translate>
            </span>
          </dt>
          <dd>{todoEntity.lastModifiedBy}</dd>
          <dt>
            <Translate contentKey="austinApp.todo.users">Users</Translate>
          </dt>
          <dd>
            {todoEntity.users
              ? todoEntity.users.map((val, i) => (
                  <span key={val.id}>
                    <a>{val.id}</a>
                    {todoEntity.users && i === todoEntity.users.length - 1 ? '' : ', '}
                  </span>
                ))
              : null}
          </dd>
        </dl>
        <Button tag={Link} to="/todo" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/todo/${todoEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default TodoDetail;
